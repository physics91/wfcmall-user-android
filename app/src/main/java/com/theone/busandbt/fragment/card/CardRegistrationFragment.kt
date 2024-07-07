package com.theone.busandbt.fragment.card

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.theone.busandbt.R
import com.theone.busandbt.adapter.card.CardViewPagerAdapter
import com.theone.busandbt.bindingadapter.CardBindingAdapter
import com.theone.busandbt.databinding.FragmentCardRegistrationBinding
import com.theone.busandbt.extension.desc
import com.theone.busandbt.fragment.DataBindingFragment
import com.theone.busandbt.listener.BackButtonInterceptor
import com.theone.busandbt.model.card.CardViewModel

class CardRegistrationFragment : DataBindingFragment<FragmentCardRegistrationBinding>(),
    BackButtonInterceptor {
    override val layoutId: Int = R.layout.fragment_card_registration
    override val actionBarTitle: String = "카드 등록"
    private val viewModel: CardViewModel by activityViewModels()
    private val fragmentList: List<Fragment> = listOf(
        CardNumberRegistrationFragment(),
        CardDateRegistrationFragment(),
        CardPasswordRegistrationFragment()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            cardRegistrationViewPager.adapter =
                CardViewPagerAdapter(childFragmentManager, lifecycle, fragmentList)
            cardRegistrationViewPager.isUserInputEnabled = false
            viewModel.currentPage.observe(viewLifecycleOwner) { page ->
                cardRegistrationViewPager.currentItem = page.value
            }
            viewModel.selectedCardTypeLiveData.observe(viewLifecycleOwner) {
                cardTextView.text = it?.desc() ?: ""
                CardBindingAdapter.setCardItemType(cardTextImageView, it?.id)
                CardBindingAdapter.cardBackground(cardImageView, it?.id)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.resetAllFields()
    }

    override fun interceptOnBackButtonClicked(): Boolean {
        val currentFragment = fragmentList[binding.cardRegistrationViewPager.currentItem]
        return !(currentFragment is BackButtonInterceptor && !currentFragment.interceptOnBackButtonClicked())
    }
}