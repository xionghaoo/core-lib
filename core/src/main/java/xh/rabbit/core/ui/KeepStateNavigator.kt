package xh.rabbit.core.ui

import android.content.Context
import android.os.Bundle
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment

@Keep
@Navigator.Name("persistent_fragment")
class KeepStateNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : Navigator<FragmentNavigator.Destination>() {

    private val backStack: ArrayDeque<Int> = ArrayDeque()
    private val animStack: ArrayDeque<NavAnimations> = ArrayDeque()

    private data class NavAnimations(
        val enter: Int,
        val exit: Int,
        val popEnter: Int,
        val popExit: Int
    )

    private fun buildAnimations(navOptions: NavOptions?): NavAnimations {
        val enter: Int = navOptions?.enterAnim ?: -1
        val exit: Int = navOptions?.exitAnim ?: -1
        val popEnter: Int = navOptions?.popEnterAnim ?: -1
        val popExit: Int = navOptions?.popExitAnim ?: -1
        return NavAnimations(
            if (enter >= 0) enter else 0,
            if (exit >= 0) exit else 0,
            if (popEnter >= 0) popEnter else 0,
            if (popExit >= 0) popExit else 0
        )
    }

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
        val animations: NavAnimations = buildAnimations(navOptions)
        if (animations.enter != 0 || animations.exit != 0 || animations.popEnter != 0 || animations.popExit != 0) {
            transaction.setCustomAnimations(animations.enter, animations.exit, animations.popEnter, animations.popExit)
        }
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
        if (!isSingleTop) {
            backStack.addLast(destination.id)
            animStack.addLast(animations)
        }
        return destination
    }

    override fun popBackStack(): Boolean {
        if (fragmentManager.isStateSaved || backStack.isEmpty()) return false
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        val current: Fragment? = fragmentManager.primaryNavigationFragment
        val lastAnimations: NavAnimations? = animStack.lastOrNull()
        if (lastAnimations != null && (lastAnimations.popEnter != 0 || lastAnimations.popExit != 0)) {
            transaction.setCustomAnimations(lastAnimations.popEnter, lastAnimations.popExit)
        }
        if (current != null) transaction.hide(current)

        backStack.removeLast()
        if (animStack.isNotEmpty()) animStack.removeLast()
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
        if (animStack.isNotEmpty()) {
            val enters: IntArray = animStack.map { it.enter }.toIntArray()
            val exits: IntArray = animStack.map { it.exit }.toIntArray()
            val popEnters: IntArray = animStack.map { it.popEnter }.toIntArray()
            val popExits: IntArray = animStack.map { it.popExit }.toIntArray()
            bundle.putIntArray("ks_anim_enter", enters)
            bundle.putIntArray("ks_anim_exit", exits)
            bundle.putIntArray("ks_anim_pop_enter", popEnters)
            bundle.putIntArray("ks_anim_pop_exit", popExits)
        }
        return bundle
    }

    override fun onRestoreState(savedState: Bundle) {
        val array: IntArray? = savedState.getIntArray("ks_back_stack")
        backStack.clear()
        if (array != null) for (id: Int in array) backStack.addLast(id)
        animStack.clear()
        val enters: IntArray? = savedState.getIntArray("ks_anim_enter")
        val exits: IntArray? = savedState.getIntArray("ks_anim_exit")
        val popEnters: IntArray? = savedState.getIntArray("ks_anim_pop_enter")
        val popExits: IntArray? = savedState.getIntArray("ks_anim_pop_exit")
        if (enters != null && exits != null && popEnters != null && popExits != null) {
            val size: Int = listOf(enters.size, exits.size, popEnters.size, popExits.size).minOrNull() ?: 0
            for (i: Int in 0 until size) {
                animStack.addLast(NavAnimations(enters[i], exits[i], popEnters[i], popExits[i]))
            }
        }
    }

    companion object {
        fun attachTo(context: Context, navHost: NavHostFragment) {
            val keepStateNavigator = KeepStateNavigator(context, navHost.childFragmentManager, navHost.id)
            val navController = navHost.navController
            navController.navigatorProvider.addNavigator(keepStateNavigator)
        }
    }
}