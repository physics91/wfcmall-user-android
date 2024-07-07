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
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.UriUtils
import com.google.android.gms.common.util.Base64Utils
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.MemberAPI
import com.theone.busandbt.databinding.FragmentMyInfoBinding
import com.theone.busandbt.dialog.CardManagementDialog
import com.theone.busandbt.dto.member.MemberDetail
import com.theone.busandbt.dto.member.request.SetMemberRequest
import com.theone.busandbt.extension.callOnSuccess
import com.theone.busandbt.extension.packageManager
import com.theone.busandbt.extension.playAlphaAnimation
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.instance.MAIN_COLOR
import com.theone.busandbt.model.my_info.MyInfoViewModel
import org.koin.android.ext.android.inject

/**
 * 내정보 화면
 * 로그인이 안돼있으면 접근할 수 없도록 한다.
 */
class MyInfoFragment : DataBindingFragment<FragmentMyInfoBinding>(), View.OnClickListener,
    EnabledGoBackButton, RequiredLogin {

    companion object {
        private const val CAPTURE_CODE = 1
        private const val OPEN_PICTURE_CODE = 2
    }

    override val layoutId: Int = R.layout.fragment_my_info
    override val actionBarTitle: String = "MY동백"
    private val memberAPI: MemberAPI by inject()
    private val myInfoViewModel: MyInfoViewModel by viewModels()
    private var imageUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            myInfoViewModel.memberDetailLiveData.observe(viewLifecycleOwner) {
                if (it == null) return@observe
                memberDetail = it
                initView(it)
            }
            if (myInfoViewModel.memberDetailLiveData.value == null) {
                safeApiRequest(
                    memberAPI.getMemberDetail(innerLoginInfo.getFormedToken(), innerLoginInfo.id),
                    showProgress = false
                ) {
                    myInfoViewModel.setMemberDetail(it)
                }
            }
        }
    }

    private fun initView(memberDetail: MemberDetail) {
        with(binding) {
            profileImageView.clipToOutline = true
            reviewTextView.setTitleWithCount("리뷰", memberDetail.reviewCount)
            couponTextView.setTitleWithCount("쿠폰", memberDetail.couponCount)
            initClickListener()
            root.playAlphaAnimation()
        }
    }

    override fun onClick(v: View?) {
        val innerLoginInfo = loginInfo ?: return
        with(binding) {
            when (v) {
                goManagement -> findNavController().navigate(R.id.action_myInfoFragment_to_myInfoManagementFragment)
                noticeListTextView -> findNavController().navigate(R.id.action_myInfoFragment_to_noticeListFragment)
                accountSetting -> findNavController().navigate(R.id.action_myInfoFragment_to_myinfoAccountSettingFragment)
                notificationSetting -> findNavController().navigate(R.id.action_myInfoFragment_to_myinfoNotificationSettingFragment)
                contactUsTextView -> findNavController().navigate(R.id.action_myInfoFragment_to_contactUsFragment)
                goMyCouponListSpace -> findNavController().navigate(R.id.action_myInfoFragment_to_myCouponFragment)
                goMyReviewListSpace -> findNavController().navigate(R.id.action_myInfoFragment_to_myReviewFragment)
                goLikeListSpace -> findNavController().navigate(R.id.action_myInfoFragment_to_myWishListFragment)
                goOrderListSpace -> findNavController().navigate(R.id.action_myInfoFragment_to_orderHistoryFragment)
                cardSetting -> CardManagementDialog().show(childFragmentManager, null)
                profileImageView -> {
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
                            profileImageView.setImageResource(R.drawable.ic_myinfo_profile_img)
                            loginInfoViewModel.update()
                        }
                        reviewImagePopup.dismiss()
                    }
                }

                else -> {}
            }
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
                            innerLoginInfo.id,
                            innerLoginInfo.getFormedToken(),
                            SetMemberRequest(file = getFile())
                        )
                    }

                    else -> {}
                }
            }

            OPEN_PICTURE_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data?.data ?: return
                        addImage(uri)
                        updateMemberProfileImage(
                            innerLoginInfo.id,
                            innerLoginInfo.getFormedToken(),
                            SetMemberRequest(file = getFile())
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
                memberId = memberId
            ).callOnSuccess {
                loginInfo?.profileImageUrl = it.profileImageUrl
                loginInfoViewModel.update()
                view?.showMessageBar("프로필 사진이 정상적으로 수정되었어요.")
            }
        }
    }

    private fun TextView.setTitleWithCount(baseText: String, count: Int) {
        text = buildSpannedString {
            append("$baseText ")
            append(
                if (count > 999) "999+" else count.toString(),
                ForegroundColorSpan(MAIN_COLOR),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun initClickListener() {
        with(binding) {
            goManagement.setOnClickListener(this@MyInfoFragment)
            noticeListTextView.setOnClickListener(this@MyInfoFragment)
            accountSetting.setOnClickListener(this@MyInfoFragment)
            notificationSetting.setOnClickListener(this@MyInfoFragment)
            contactUsTextView.setOnClickListener(this@MyInfoFragment)
            goMyCouponListSpace.setOnClickListener(this@MyInfoFragment)
            goMyReviewListSpace.setOnClickListener(this@MyInfoFragment)
            goLikeListSpace.setOnClickListener(this@MyInfoFragment)
            goOrderListSpace.setOnClickListener(this@MyInfoFragment)
            profileImageView.setOnClickListener(this@MyInfoFragment)
            cardSetting.setOnClickListener(this@MyInfoFragment)
        }
    }

    private fun getFile(): SetMemberRequest.ProfileImageFile? {
        val i = imageUri ?: return null
        val file = UriUtils.uri2File(i)
        val bitmap = ImageUtils.getBitmap(file) ?: return null
        val encodedFile = Base64Utils.encode(ImageUtils.compressByQuality(bitmap, 50, true))
        return SetMemberRequest.ProfileImageFile(encodedFile, file.name, file.extension)
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
}