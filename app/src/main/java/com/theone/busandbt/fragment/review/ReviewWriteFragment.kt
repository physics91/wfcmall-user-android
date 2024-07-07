package com.theone.busandbt.fragment.review

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.UriUtils
import com.google.android.gms.common.util.Base64Utils
import com.theone.busandbt.R
import com.theone.busandbt.adapter.review.ReviewWriteImageListAdapter
import com.theone.busandbt.adapter.review.ReviewWriteMenuListAdapter
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.addon.RequiredLogin
import com.theone.busandbt.api.orderchannel.OrderAPI
import com.theone.busandbt.api.orderchannel.ReviewAPI
import com.theone.busandbt.databinding.FragmentReviewWriteBinding
import com.theone.busandbt.dto.review.request.AddReviewRequest
import com.theone.busandbt.extension.safeApiRequest
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.extension.showMessageDialog
import com.theone.busandbt.item.ReviewWriteImage
import org.koin.android.ext.android.inject

/**
 * 리뷰 작성 화면
 */
class ReviewWriteFragment : BaseReviewModifyFragment<FragmentReviewWriteBinding>(),
    EnabledGoBackButton, RequiredLogin, View.OnClickListener {
    override val layoutId: Int = R.layout.fragment_review_write
    override val actionBarTitle: String = "리뷰쓰기"
    override val reviewImageRecyclerView: RecyclerView get() = binding.imageList
    override val reviewImageListAdapter: ReviewWriteImageListAdapter by lazy {
        ReviewWriteImageListAdapter()
    }
    override val reviewMenuRecyclerView: RecyclerView get() = binding.menuList
    override val reviewMenuListAdapter: ReviewWriteMenuListAdapter by lazy { ReviewWriteMenuListAdapter() }
    override val reviewImageCountTextView: TextView get() = binding.reviewImageCountTextView
    override val addReviewImageButton: View get() = binding.imageAddButton
    private val args by navArgs<ReviewWriteFragmentArgs>()
    private val reviewAPI: ReviewAPI by inject()
    private val orderAPI: OrderAPI by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val innerLoginInfo = loginInfo ?: return
        safeApiRequest(
            orderAPI.getOrderDetail(innerLoginInfo.getFormedToken(), args.orderId)
        ) {
            with(binding) {
                orderDetail = it
                if (it.doWrittenReview) {
                    view.showMessageBar("이미 리뷰를 작성한 주문입니다.")
                    findNavController().popBackStack()
                    return@with
                }
                imageList.layoutManager = LinearLayoutManager(view.context).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                reviewMenuListAdapter.addItems(it.menuList.distinctBy { m -> m.menuId })
                writeButton.setOnClickListener(this@ReviewWriteFragment)
            }
        }
    }

    override fun onClick(v: View?) {
        with(binding) {
            val od = orderDetail ?: return
            when (v) {
                writeButton -> {
                    val innerLoginInfo = loginInfo ?: return
                    val content = (reviewContentEditText.text?.toString() ?: "").trim()
                    if (content.length < 10) {
                        showMessageDialog("최소 10자 이상 입력해 주세요.", showWarningImageView = true) {
                            onDoneButtonClick { dismiss() }
                        }
                        return
                    }

                    safeApiRequest(
                        reviewAPI.addReview(
                            innerLoginInfo.getFormedToken(),
                            AddReviewRequest(
                                shopId = od.shopId,
                                orderId = args.orderId,
                                writerId = innerLoginInfo.id,
                                content = content,
                                star = reviewRatingBar.rating.toDouble(),
                                isVisible = !visibleCheckBox.isChecked,
                                menuList = getMenuList(),
                                fileList = getFileList()
                            )
                        )
                    ) {
                        innerLoginInfo.reviewCount += 1
                        loginInfoViewModel.update()
                        showMessageDialog("리뷰를 정상적으로 작성했어요") {
                            onDoneButtonClick {
                                dismiss()
                            }
                        }
                        setResultData(bundleOf("orderId" to args.orderId, "doWrittenReview" to true))
                        findNavController().popBackStack()
                    }
                }

                else -> {}
            }
        }
    }

    override fun addImage(imageUri: Uri) {
        val adapter = reviewImageListAdapter
        adapter.addItem(ReviewWriteImage(imageUri))
        refreshReviewImageCount()
    }

    private fun getFileList(): List<AddReviewRequest.ReviewFile> {
        val adapter =
            reviewImageListAdapter
        val result = ArrayList<AddReviewRequest.ReviewFile>()
        adapter.items.forEach {
            val file = UriUtils.uri2File(it.imageUri)
            val bitmap = ImageUtils.getBitmap(file) ?: return@forEach
            ImageUtils.compressByScale(bitmap, 0.1f, 0.1f)
            val encodedFile = Base64Utils.encode(ImageUtils.compressByQuality(bitmap, 10, true))
            result.add(AddReviewRequest.ReviewFile(encodedFile, file.name, file.extension))
        }
        return result
    }

    private fun getMenuList(): List<AddReviewRequest.ReviewMenu> {
        val adapter = reviewMenuListAdapter
        return adapter.items.map {
            AddReviewRequest.ReviewMenu(
                it.menuId,
                adapter.getRecommend(it) ?: false
            )
        }
    }
}