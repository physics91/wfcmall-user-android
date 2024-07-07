package com.theone.busandbt.fragment.review

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.UriUtils
import com.google.android.gms.common.util.Base64Utils
import com.theone.busandbt.R
import com.theone.busandbt.adapter.review.SetReviewImageListAdapter
import com.theone.busandbt.adapter.review.SetReviewMenuListAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.ReviewAPI
import com.theone.busandbt.databinding.FragmentSetReviewBinding
import com.theone.busandbt.dto.review.request.AddReviewRequest
import com.theone.busandbt.dto.review.request.SetReviewRequest
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.item.SetReviewImage
import org.koin.android.ext.android.inject

class SetReviewFragment : BaseReviewModifyFragment<FragmentSetReviewBinding>(),
    EnabledGoBackButton, RequiredLogin, View.OnClickListener {

    override val layoutId: Int = R.layout.fragment_set_review
    override val actionBarTitle: String = "리뷰 수정하기"
    override val reviewImageRecyclerView: RecyclerView get() = binding.imageList
    override val reviewImageListAdapter: SetReviewImageListAdapter by lazy {
        SetReviewImageListAdapter(
            args.review.imageFileList
        )
    }
    override val reviewMenuRecyclerView: RecyclerView get() = binding.menuList
    override val reviewMenuListAdapter: SetReviewMenuListAdapter by lazy {
        SetReviewMenuListAdapter(
            args.review.menuList
        )
    }
    override val reviewImageCountTextView: TextView get() = binding.reviewImageCountTextView
    override val addReviewImageButton: View get() = binding.imageAddButton
    private val args by navArgs<SetReviewFragmentArgs>()
    private val reviewAPI: ReviewAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            review = args.review
            imageList.layoutManager = LinearLayoutManager(view.context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            reviewContentEditText.setText(args.review.content)
            visibleCheckBox.isChecked = !args.review.isVisible
            reviewRatingBar.rating = args.review.star.toFloat()
            writeButton.setOnClickListener(this@SetReviewFragment)
            refreshReviewImageCount()
        }
    }

    override fun onClick(v: View?) {
        with(binding) {
            when (v) {
                writeButton -> {
                    val innerLoginInfo = loginInfo ?: return
                    val currentContent = (reviewContentEditText.text?.toString() ?: "").trim()
                    if (currentContent.length < 10) {
                        showMessageDialog("최소 10자 이상 입력해 주세요.", showWarningImageView = true) {
                            onDoneButtonClick { dismiss() }
                        }
                        return
                    }
                    val isLimitVisible = visibleCheckBox.isChecked
                    val star =
                        if (args.review.star != reviewRatingBar.rating.toDouble()) reviewRatingBar.rating.toDouble() else null
                    val content =
                        if (args.review.content != currentContent) currentContent else null
                    val visible =
                        if (args.review.isVisible != isLimitVisible) !isLimitVisible else null
                    safeApiRequest(
                        reviewAPI.setReview(
                            innerLoginInfo.getFormedToken(),
                            args.review.id,
                            SetReviewRequest(
                                newImageList = getNewFileList(),
                                removedImageIdList = getRemovedFileIdList(),
                                changedMenuList = getChangedMenuList(),
                                star = star,
                                content = content,
                                isVisible = visible
                            )
                        )
                    ) {
                        showMessageDialog("리뷰를 정상적으로 수정했어요") {
                            onDoneButtonClick {
                                dismiss()
                            }
                        }
                        findNavController().popBackStack()
                    }
                }

                else -> {}
            }
        }
    }

    override fun addImage(imageUri: Uri) {
        val adapter = reviewImageListAdapter
        adapter.addItem(SetReviewImage(imageUri))
    }

    private fun getNewFileList(): List<AddReviewRequest.ReviewFile> {
        val adapter = reviewImageListAdapter
        val result = ArrayList<AddReviewRequest.ReviewFile>()
        adapter.items.forEach {
            if (it.imageUri == null) return@forEach
            val file = UriUtils.uri2File(it.imageUri)
            val bitmap = ImageUtils.getBitmap(file) ?: return@forEach
            val encodedFile = Base64Utils.encode(ImageUtils.compressByQuality(bitmap, 50, true))
            result.add(AddReviewRequest.ReviewFile(encodedFile, file.name, file.extension))
        }
        return result
    }

    private fun getRemovedFileIdList(): List<Int> {
        val adapter =
            binding.imageList.adapter as? SetReviewImageListAdapter ?: return emptyList()
        val old = args.review.imageFileList
        val current = adapter.items.filter { it.reviewFile != null }.map { it.reviewFile }
        return old.minus(current.toSet()).map { it?.id ?: 0 }
    }

    private fun getChangedMenuList(): List<AddReviewRequest.ReviewMenu> {
        val adapter = reviewMenuListAdapter
        val old = args.review.menuList
        val result = ArrayList<AddReviewRequest.ReviewMenu>()
        adapter.items.forEach {
            val solution =
                old.find { menu -> menu.menuId == it.menuId && menu.recommended != it.recommended }
            if (solution != null) result.add(
                AddReviewRequest.ReviewMenu(
                    solution.menuId,
                    solution.recommended
                )
            )
        }
        return result
    }
}