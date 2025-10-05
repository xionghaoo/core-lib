package xh.rabbit.core.ui

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class KeepStateNavHostFragment : NavHostFragment() {

    override fun onCreateNavController(navController: NavController) {
        super.onCreateNavController(navController)
        val navigator: KeepStateNavigator = KeepStateNavigator(requireContext(), childFragmentManager, id)
        navController.navigatorProvider.addNavigator(navigator)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}


