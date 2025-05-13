package org.renxo.deeplinkapplication.navigation

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json


private const val NavResult = "N_a_V__Result_Code"

enum class NavigationResults {
    ResultOK, ResultCanceled, ResultNotOk
}


fun NavBackStackEntry.getNavResult(): NavigationResults? {
    return getValue(NavResult)
}


fun NavHostController.setResults(
    navResult: NavigationResults,
    finish: Boolean = false,
    sendExtras: NavigationResult.() -> Unit = {},
) {
    val results = NavigationResult()
    results.apply(sendExtras)
    previousBackStackEntry?.savedStateHandle?.set(NavResult, navResult)
    results.getExtras().forEach {
        previousBackStackEntry?.savedStateHandle?.set(it.key, it.value)
    }

    if (finish) {
        if (previousBackStackEntry?.id != null) {
            popBackStack()
        } else {
//            (this.context as Activity?)?.finish()
        }
    }

}

fun NavHostController.navigateTo(
    routs: NavRouts,
    finish: Boolean = false,
    finishAll: Boolean = false,
    singleTop: Boolean = true,
) {
    Log.d("navigateTo", ": $routs")
    val current = currentDestination?.route ?: this.graph.startDestinationRoute
    navigate(routs) {
        launchSingleTop = singleTop
//        restoreState = true
        if (finishAll) {
            popUpTo(graph.id) {
                inclusive = true
            }

        } else if (finish) {
            current?.let {
                popUpTo(it) {
                    inclusive = true

                }
            }
        }
    }

}

fun NavHostController.isCurrentDestination(routs: NavRouts): Boolean {
    return (currentDestination?.route?.contains(routs::class.qualifiedName.toString())) == true

}

fun NavHostController.finish(route: NavRouts) {
    this.popBackStack(route, inclusive = true)
}

fun NavHostController.finish() {
    if (previousBackStackEntry?.id != null) {
        popBackStack()
    } else {
        (this.context as Activity?)?.finish()
    }

}


fun NavHostController.finishAll() {
    if ((currentDestination?.id ?: 0) > 0) {
        this.popBackStack(graph.id, true)
    }
}


inline fun <reified T : Any> NavBackStackEntry.getValue(key: String): T? {
    if (this.savedStateHandle.contains(key)) {
        val k = savedStateHandle.get<Any>(key)
        if (k is T) {
            savedStateHandle.remove<T>(key)
            return k
        }

    }
    return null
}

inline fun <reified T : Any> NavHostController.getValue(key: String): T? {
    if (this.currentBackStackEntry?.savedStateHandle?.contains(key) == true) {
        val k = currentBackStackEntry?.savedStateHandle?.get<Any>(key)
        if (k is T) {
            currentBackStackEntry?.savedStateHandle?.remove<T>(key)
            return k
        }

    }
    return null
}


class NavigationResult {
    data class PutExtra(val key: String, val value: Any?)

    private val list = ArrayList<PutExtra>()
    fun putExtra(key: String, value: Any) {
        list.add(PutExtra(key, value))
    }

    fun getExtras() = list
}


//  These are the old function designed for helping the navigation but now all of the work can be done with get value function

fun <T> NavHostController.getLiveData(key: String, initialValue: T? = null): MutableLiveData<T>? {
    return if (initialValue != null) {
        currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key, initialValue)
    } else {
        currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)
    }
}

fun <T> NavHostController.getData(key: String): T? {
    return currentBackStackEntry?.savedStateHandle?.get<T>(key)
}

fun <T> NavHostController.getStateFlow(key: String, initValue: T): StateFlow<T>? {
    return currentBackStackEntry?.savedStateHandle?.getStateFlow<T>(key, initValue)
}


fun NavHostController.getStringExtra(key: String): String? {
    return this.currentBackStackEntry?.arguments?.getString(key)
}

fun NavHostController.getFloatExtra(key: String): Float? {
    return this.currentBackStackEntry?.arguments?.getFloat(key)

}

fun NavHostController.getIntExtra(key: String): Int? {
    return this.currentBackStackEntry?.arguments?.getInt(key, 0)
}

fun NavHostController.getBooleanExtra(key: String): Boolean? {
    return this.currentBackStackEntry?.arguments?.getBoolean(key, false)
}

inline fun <reified T : Any> NavHostController.getCustomModel(key: String): T? {

    return this.currentBackStackEntry?.arguments?.getString(key)
        ?.let { Json.decodeFromString<T>(it) }

}


