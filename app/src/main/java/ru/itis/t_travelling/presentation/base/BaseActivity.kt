package ru.itis.t_travelling.presentation.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseActivity : AppCompatActivity() {
    protected abstract val mainContainerId: Int

    fun navigate(
        destination: Fragment,
        destinationTag: String? = null,
        action: NavigationAction,
        isAddToBackStack: Boolean = true,
        backStackTag: String? = null
    ) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.slide_out_right,)

        when (action) {
            NavigationAction.ADD -> transaction.add(mainContainerId, destination, destinationTag)
            NavigationAction.REPLACE -> transaction.replace(mainContainerId, destination, destinationTag)
            NavigationAction.REMOVE -> transaction.remove(destination)
        }

        if(isAddToBackStack) {
            transaction.addToBackStack(backStackTag)
        }
        transaction.commit()
    }
}