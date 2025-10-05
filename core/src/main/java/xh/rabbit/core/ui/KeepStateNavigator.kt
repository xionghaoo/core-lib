package xh.rabbit.core.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

@Navigator.Name("persistent_fragment")
class KeepStateNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : Navigator<FragmentNavigator.Destination>() {

    private val backStack: ArrayDeque<Int> = ArrayDeque()

    override fun createDestination(): FragmentNavigator.Destination {
        return FragmentNavigator.Destination(this)
    }

    override fun navigate(
        destination: FragmentNavigator.Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        if (fragmentManager.isStateSaved) return null
        val tag: String = destination.id.toString()
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val current: Fragment? = fragmentManager.primaryNavigationFragment
        if (current != null) transaction.hide(current)

        var next: Fragment? = fragmentManager.findFragmentByTag(tag)
        if (next == null) {
            val className: String = destination.className
            next = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
            if (args != null) next.arguments = args
            transaction.add(containerId, next, tag)
        } else {
            transaction.show(next)
        }

        transaction.setPrimaryNavigationFragment(next)
        transaction.setReorderingAllowed(true)
        transaction.commit()

        val isSingleTop: Boolean = navOptions?.shouldLaunchSingleTop() == true && backStack.lastOrNull() == destination.id
        if (!isSingleTop) backStack.addLast(destination.id)
        return destination
    }

    override fun popBackStack(): Boolean {
        if (fragmentManager.isStateSaved || backStack.isEmpty()) return false
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val current: Fragment? = fragmentManager.primaryNavigationFragment
        if (current != null) transaction.hide(current)

        backStack.removeLast()
        val previousId: Int? = backStack.lastOrNull()
        if (previousId != null) {
            val previous: Fragment? = fragmentManager.findFragmentByTag(previousId.toString())
            if (previous != null) {
                transaction.show(previous)
                transaction.setPrimaryNavigationFragment(previous)
            }
        }
        transaction.setReorderingAllowed(true)
        transaction.commit()
        return true
    }

    override fun onSaveState(): Bundle {
        val bundle: Bundle = Bundle()
        bundle.putIntArray("ks_back_stack", backStack.toIntArray())
        return bundle
    }

    override fun onRestoreState(savedState: Bundle) {
        val array: IntArray? = savedState.getIntArray("ks_back_stack")
        backStack.clear()
        if (array != null) for (id: Int in array) backStack.addLast(id)
    }
}