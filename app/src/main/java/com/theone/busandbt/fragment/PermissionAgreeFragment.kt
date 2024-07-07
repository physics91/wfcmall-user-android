package com.theone.busandbt.fragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.theone.busandbt.R
import com.theone.busandbt.addon.EnabledGoBackButton
import com.theone.busandbt.databinding.FragmentPermissionAgreeBinding
import com.theone.busandbt.fragment.join.BaseJoinFragment

/**
 * 회원가입 - 권한 허용 화면
 */
class PermissionAgreeFragment : BaseJoinFragment<FragmentPermissionAgreeBinding>(),
    EnabledGoBackButton {
    override val layoutId: Int = R.layout.fragment_permission_agree

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            nextButton.setOnClickListener {
                with(findNavController()) {
                    navigate(R.id.action_permissionAgreeFragment_to_serviceAgreeFragment)
                }
            }
        }
    }
}