package com.theone.busandbt.fragment.review

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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.theone.busandbt.R
import com.theone.busandbt.adapter.DataBindingListAdapter
import com.theone.busandbt.addon.EventBusSubscriber
import com.theone.busandbt.eventbus.RemoveReviewImageEvent
import com.theone.busandbt.extension.packageManager
import com.theone.busandbt.extension.showMessageBar
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.utils.MAX_REVIEW_IMAGE_COUNT
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseReviewModifyFragment<VDB : ViewDataBinding> : DataBindingFragment<VDB>(),
    EventBusSubscriber {

    protected abstract val reviewImageRecyclerView: RecyclerView
    protected abstract val reviewImageListAdapter: DataBindingListAdapter<*, *>
    protected abstract val reviewMenuRecyclerView: RecyclerView
    protected abstract val reviewMenuListAdapter: DataBindingListAdapter<*, *>
    protected abstract val addReviewImageButton: View
    protected abstract val reviewImageCountTextView: TextView
    private var imageUri: Uri? = null
    private val openCameraActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        addImage(imageUri ?: return@registerForActivityResult)
        refreshReviewImageCount()
    }

    private val openGalleryActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        val uri = it.data?.data ?: return@registerForActivityResult
        addImage(uri)
        refreshReviewImageCount()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewImageRecyclerView.adapter = reviewImageListAdapter
        reviewMenuRecyclerView.adapter = reviewMenuListAdapter
        addReviewImageButton.setOnClickListener {
            val adapter = reviewImageListAdapter
            val imageCount = adapter.itemCount
            if (imageCount >= MAX_REVIEW_IMAGE_COUNT) {
                view.showMessageBar(getString(R.string.reviewImageLimitOverMessage))
                return@setOnClickListener
            }
            val popupLayout =
                LayoutInflater.from(context).inflate(R.layout.popup_review_image, null)
            val builder = AlertDialog.Builder(context)
                .setView(popupLayout)
            val reviewImagePopup = builder.show()
            reviewImagePopup.window
                ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            //팝업창 확인버튼 , 텍스트 가져오기
            val goGalleryButton = popupLayout?.findViewById<TextView>(R.id.goGalleryButton)
            val goCameraButton = popupLayout?.findViewById<TextView>(R.id.goCameraButton)
            goGalleryButton?.setOnClickListener {
                reviewImagePopup.dismiss()
                openGallery()
            }
            goCameraButton?.setOnClickListener {
                reviewImagePopup.dismiss()
                openCamera()
            }
        }
    }

    protected abstract fun addImage(imageUri: Uri)

    /**
     * 카메라 호출
     */
    protected fun openCamera() {
        val p = packageManager ?: return
        val c = activity?.contentResolver ?: return
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(p)?.also { _ ->
                imageUri = c.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues()
                ) ?: return
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                openCameraActivityResult.launch(intent)
            }
        }
    }

    /**
     * 갤러리 호출
     */
    protected fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        openGalleryActivityResult.launch(intent)
    }

    protected fun refreshReviewImageCount() {
        reviewImageCountTextView.text =
            "${reviewImageListAdapter.items.size}/$MAX_REVIEW_IMAGE_COUNT"
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRemoveReviewImage(event: RemoveReviewImageEvent) {
        refreshReviewImageCount()
    }
}