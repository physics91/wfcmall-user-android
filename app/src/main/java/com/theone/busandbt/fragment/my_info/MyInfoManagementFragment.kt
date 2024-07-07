package com.theone.busandbt.fragment.my_info

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.UriUtils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.loginchannel.LoginAPI
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.databinding.FragmentMyInfoManagementBinding
import com.theone.busandbt.dto.login.request.SecessionRequest
import com.theone.busandbt.dto.member.request.SetMemberRequest
import com.theone.busandbt.extension.*
import com.theone.busandbt.fragment.DataBindingFragment
import com.busandbt.code.JoinType
import com.google.android.gms.common.util.Base64Utils
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import org.koin.android.ext.android.inject

/**
 * 내정보 관리 페이지
 */
class MyInfoManagementFragment : DataBindingFragment<FragmentMyInfoManagementBinding>(),
    View.OnClickListener,
    RequiredLogin, EnabledGoBackButton {

    companion object {
        private const val CAPTURE_CODE = 1
        private const val OPEN_PICTURE_CODE = 2
    }

    override val layoutId: Int = R.layout.fragment_my_info_management
    override val actionBarTitle: String = "MY동백"
    private lateinit var myInfoPopupWindow: AlertDialog
    private val memberAPI: MemberAPI by inject()
    private var imageUri: Uri? = null
    private val loginAPI: LoginAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginInfo = loginInfoViewModel.getLoginInfo() ?: return
        with(binding) {
            this.loginInfo = loginInfo
            profileImageView.clipToOutline = true
            goEditMyInfoTextView.setOnClickListener(this@MyInfoManagementFragment)
            logoutTextView.setOnClickListener(this@MyInfoManagementFragment)
            profileImageView.setOnClickListener(this@MyInfoManagementFragment)
            withdrawalTextView.setOnClickListener(this@MyInfoManagementFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val innerLoginInfo = loginInfo ?: return
        when (requestCode) {
            CAPTURE_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val i = imageUri ?: return
                        addImage(i)
                        updateMemberProfileImage(
                            token = innerLoginInfo.getFormedToken(),
                            memberId = innerLoginInfo.id,
                            request = SetMemberRequest(file = getFile())
                        )
                    }
                    else -> {}
                }
            }
            OPEN_PICTURE_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data?.data ?: return
                        imageUri = uri
                        addImage(uri)
                        updateMemberProfileImage(
                            token = innerLoginInfo.getFormedToken(),
                            memberId = innerLoginInfo.id,
                            request = SetMemberRequest(file = getFile())
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    // TODO - 여기에서 이 프래그먼트 라이프사이클이 destroy 될 경우에 로그인 정보에 버그가 생기는지 확인 필요
    private fun updateMemberProfileImage(
        memberId: Int,
        token: String,
        request: SetMemberRequest
    ) {
        memberAPI.setMember(
            token = token,
            memberId = memberId,
            request = request
        ).callOnSuccess(showFailMessage = true) {
            memberAPI.getMemberDetail(
                token = token,
                memberId = memberId,
            ).callOnSuccess {
                loginInfo?.profileImageUrl = it.profileImageUrl
                loginInfoViewModel.update()
                view?.showMessageBar("프로필 사진이 정상적으로 수정되었어요.")
            }
        }
    }

    //레이아웃을 받아 팝업창 띄우는 함수
    private fun showPopup(layoutName: Int): View? {
        val popupLayout = LayoutInflater.from(context).inflate(layoutName, null)
        val builder = AlertDialog.Builder(context)
            .setView(popupLayout)
        myInfoPopupWindow = builder.show()
        myInfoPopupWindow.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return popupLayout
    }

    // 똑같이 btnPopup 사용했을 때 뒤에 팝업창이 남아있어 함수를 하나 더 만들어 팝업창을 띄우고 뒤에 팝업창을 없애줬습니다
    private fun dismissBtnPopup(layoutName: Int, confirm: Int, cancellation: Int) {
        dialogDismiss()
        val popupLayout = showPopup(layoutName)
        val confirmBtn = popupLayout?.findViewById<Button>(confirm)
        val cancellationBtn = popupLayout?.findViewById<Button>(cancellation)
        confirmBtn?.setOnClickListener(this@MyInfoManagementFragment)
        cancellationBtn?.setOnClickListener(this@MyInfoManagementFragment)
    }

    private fun getFile(): SetMemberRequest.ProfileImageFile? {
        val i = imageUri ?: return null
        val file = UriUtils.uri2File(i)
        val bitmap = ImageUtils.getBitmap(file) ?: return null
        ImageUtils.compressByScale(bitmap, 0.1f, 0.1f)
        val compressByteArray = ImageUtils.compressByQuality(bitmap, 10, true)
        val encodedFile = Base64Utils.encode(compressByteArray)
        return SetMemberRequest.ProfileImageFile(
            encodedFile,
            file.nameWithoutExtension,
            file.extension
        )
    }

    private fun addImage(imageUri: Uri) {
        binding.profileImageView.setImageURI(imageUri)
    }

    /**
     * 카메라 호출
     */
    private fun openCamera() {
        val p = packageManager ?: return
        val c = activity?.contentResolver ?: return
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(p)?.also { _ ->
                imageUri = c.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                ) ?: return
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(intent, CAPTURE_CODE)
            }
        }
    }

    /**
     * 갤러리 호출
     */
    private fun openGallery() {
        val p = packageManager ?: return
        Intent(Intent.ACTION_PICK).also { intent ->
            intent.resolveActivity(p)?.also { _ ->
                intent.type = "image/*"
                intent.putExtra(
                    "android.intent.extra.MIME_TYPES",
                    arrayOf("image/jpeg", "image/png")
                )
                startActivityForResult(intent, OPEN_PICTURE_CODE)
            }
        }
    }

    // 레이아웃이랑 버튼 아이디를 받고  팝업창 네비로 이동한 후  띄워주기 버튼 클릭시 팝업창 닫기
    private fun movePopup(layoutName: Int, confirm: Int) {
        dialogDismiss()
        val popupLayout = showPopup(layoutName)
        val confirmBtn = popupLayout?.findViewById<Button>(confirm)
        confirmBtn?.setOnClickListener {
            dialogDismiss()
            findNavController().navigate(R.id.action_myInfoManagementFragment_to_homeFragment)
        }
    }

    // 팝업창을 꺼주는 함수
    private fun dialogDismiss() {
        if (::myInfoPopupWindow.isInitialized) {
            myInfoPopupWindow.dismiss()
        }
    }

    override fun onClick(view: View?) {
        val innerLoginInfo = loginInfo ?: return
        when (view?.id) {
            //회원정보 수정창으로 이동하는 이벤트
            R.id.goEditMyInfoTextView -> findNavController().navigate(R.id.action_myInfoManagementFragment_to_editMyInfoFragment)

            //로그아웃 팝업 생성
            R.id.logoutTextView -> {
                showMessageDialog(
                    "로그아웃 하시겠어요?",
                    showWarningImageView = false,
                    showCancelButton = true
                ) {
                    onDoneButtonClick(buttonText = "로그아웃") {
                        loginInfoViewModel.logout()
                        view.showMessageBar("로그아웃 되었습니다.")
                        when (innerLoginInfo.joinType) {
                            JoinType.NAVER.id -> {
                                NidOAuthLogin().callDeleteTokenApi(view.context, object :
                                    OAuthLoginCallback {
                                    override fun onSuccess() {
                                        //서버에서 토큰 삭제에 성공한 상태입니다.
                                        debugLog("naver", "토큰 삭제 완료")
                                    }

                                    override fun onFailure(httpStatus: Int, message: String) {
                                        // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                                        // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                                        debugLog(
                                            "naver",
                                            "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}"
                                        )
                                        debugLog(
                                            "naver",
                                            "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}"
                                        )
                                    }

                                    override fun onError(errorCode: Int, message: String) {
                                        // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                                        // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                                        onFailure(errorCode, message)
                                    }
                                })
                            }
                            else -> {}
                        }
                        findNavController().navigate(R.id.action_myInfoManagementFragment_to_homeFragment)
                        dismiss()
                    }
                }
            }

            //프로필수정 팝업 생성 이벤트
            R.id.profileImageView -> {
                val popupLayout =
                    LayoutInflater.from(context)
                        .inflate(R.layout.popup_myinfo_profile_change, null)
                val builder = AlertDialog.Builder(context)
                    .setView(popupLayout)
                val reviewImagePopup = builder.show()
                reviewImagePopup.window
                    ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                //팝업창 확인버튼 , 텍스트 가져오기
                val goGalleryButton = popupLayout?.findViewById<TextView>(R.id.goGalleryButton)
                val goCameraButton = popupLayout?.findViewById<TextView>(R.id.goCameraButton)
                val removeProfileImageButton =
                    popupLayout?.findViewById<TextView>(R.id.removeProfileImageButton)
                goGalleryButton?.setOnClickListener {
                    reviewImagePopup.dismiss()
                    openGallery()
                }
                goCameraButton?.setOnClickListener {
                    reviewImagePopup.dismiss()
                    openCamera()
                }
                removeProfileImageButton?.setOnClickListener {
                    safeApiRequest(
                        memberAPI.setMember(
                            innerLoginInfo.getFormedToken(),
                            innerLoginInfo.id,
                            SetMemberRequest(doRemoveProfileImage = true)
                        )
                    ) {
                        loginInfo?.profileImageUrl = ""
                        binding.profileImageView.setImageResource(R.drawable.ic_myinfo_profile_img)
                        loginInfoViewModel.update()
                    }
                    view.showMessageBar("프로필 사진이 정상적으로 삭제되었어요.")
                    reviewImagePopup.dismiss()
                }
            }


            //회원탈퇴 팝업 생성 TODO:취소 버튼 텍스트 변경해줘야함
            R.id.withdrawalTextView -> {
                showMessageDialog(
                    "정말 탈퇴하시겠어요?", "탈퇴 시 모든 계정 정보가 삭제되고 되돌릴 수\n" +
                            "없습니다. 보유하신 쿠폰은 모두 소멸됩니다.",
                    showWarningImageView = false,
                    showCancelButton = true
                ) {
                    onDoneButtonClick(buttonText = "탈퇴 할래요") {
                        when (innerLoginInfo.joinType) {
                            JoinType.NORMAL.id -> {
                                dismissBtnPopup(
                                    R.layout.popup_withdrawal_password,
                                    R.id.passwordOk,
                                    R.id.cancellationBtn
                                )
                                dismiss()
                            }
                            else -> {
                                safeApiRequest(
                                    loginAPI.secession(
                                        innerLoginInfo.getFormedToken(),
                                        SecessionRequest(innerLoginInfo.id, null)
                                    ),
                                    showFailMessage = true
                                ) {
                                    loginInfoViewModel.logout()
                                    movePopup(R.layout.popup_withdrawal_clear, R.id.confirmBtn)
                                }
                            }
                        }
                    }
                }
            }

            //패스워드 확인시 팝업생성
            R.id.passwordOk -> {
                val passwordForm =
                    (view.parent as ViewGroup).findViewById<EditText>(R.id.confirmPassword)
                val passwordErrorText =
                    (view.parent as ViewGroup).findViewById<TextView>(R.id.errorMessage)
                val inputPassword = passwordForm.text.toString().trim()
                if (inputPassword.isEmpty()) return
                safeApiRequest(
                    loginAPI.secession(
                        innerLoginInfo.getFormedToken(),
                        SecessionRequest(innerLoginInfo.id, inputPassword)
                    ),
                    onFail = { _, _ ->
                        passwordErrorText.isVisible = true
                    }
                ) {
                    loginInfoViewModel.logout()
                    movePopup(R.layout.popup_withdrawal_clear, R.id.confirmBtn)
                }
            }
            //취소시 이벤트
            R.id.cancellationBtn -> dialogDismiss()
        }
    }
}