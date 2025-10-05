/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xh.rabbit.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Parcel
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xh.rabbit.core.network.RemoteRequestStrategy
import xh.rabbit.core.vo.ApiResponse
import xh.rabbit.core.vo.Resource
import xh.rabbit.core.vo.Status
import xh.rabbit.core.widgets.NetworkStateLayout

/**
 * Implementation of lazy that is not thread safe. Useful when you know what thread you will be
 * executing on and are not worried about synchronization.
 */
fun <T> lazyFast(operation: () -> T): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    operation()
}

/** Convenience for callbacks/listeners whose return value indicates an event was consumed. */
inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

/**
 * Allows calls like
 *
 * `viewGroup.inflate(R.layout.foo)`
 */
fun ViewGroup.inflate(@LayoutRes layout: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layout, this, attachToRoot)
}

/**
 * Allows calls like
 *
 * `supportFragmentManager.inTransaction { add(...) }`
 */
inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

// region ViewModels

/**
 * For Actvities, allows declarations like
 * ```
 * val myViewModel = viewModelProvider(myViewModelFactory)
 * ```
 */
inline fun <reified VM : ViewModel> FragmentActivity.viewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(this, provider).get(VM::class.java)
//    ViewModelProviders.of(this, provider).get(VM::class.java)

/**
 * For Fragments, allows declarations like
 * ```
 * val myViewModel = viewModelProvider(myViewModelFactory)
 * ```
 */
inline fun <reified VM : ViewModel> Fragment.viewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(this, provider).get(VM::class.java)
//    ViewModelProviders.of(this, provider).get(VM::class.java)

/**
 * Like [Fragment.viewModelProvider] for Fragments that want a [ViewModel] scoped to the Activity.
 */
inline fun <reified VM : ViewModel> Fragment.activityViewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(requireActivity(), provider).get(VM::class.java)
//    ViewModelProviders.of(requireActivity(), provider).get(VM::class.java)

/**
 * Like [Fragment.viewModelProvider] for Fragments that want a [ViewModel] scoped to the parent
 * Fragment.
 */
inline fun <reified VM : ViewModel> Fragment.parentViewModelProvider(
    provider: ViewModelProvider.Factory
) = ViewModelProvider(parentFragment!!, provider).get(VM::class.java)
//    ViewModelProviders.of(parentFragment!!, provider).get(VM::class.java)

// endregion
// region Parcelables, Bundles

/** Write an enum value to a Parcel */
fun <T : Enum<T>> Parcel.writeEnum(value: T) = writeString(value.name)

/** Read an enum value from a Parcel */
//inline fun <reified T : Enum<T>> Parcel.readEnum(): T = enumValueOf(readString())

/** Write an enum value to a Bundle */
fun <T : Enum<T>> Bundle.putEnum(key: String, value: T) = putString(key, value.name)

/** Read an enum value from a Bundle */
//inline fun <reified T : Enum<T>> Bundle.getEnum(key: String): T = enumValueOf(getString(key))

/** Write a boolean to a Parcel (copied from Parcel, where this is @hidden). */
fun Parcel.writeBoolean(value: Boolean) = writeInt(if (value) 1 else 0)

/** Read a boolean from a Parcel (copied from Parcel, where this is @hidden). */
fun Parcel.readBoolean() = readInt() != 0

// endregion
// region LiveData

fun <T> MutableLiveData<T>.setValueIfNew(newValue: T) {
    if (this.value != newValue) value = newValue
}

fun <T> MutableLiveData<T>.postValueIfNew(newValue: T) {
    if (this.value != newValue) postValue(newValue)
}
// endregion

// region ZonedDateTime
//fun ZonedDateTime.toEpochMilli() = this.toInstant().toEpochMilli()
// endregion

/**
 * Helper to force a when statement to assert all options are matched in a when statement.
 *
 * By default, Kotlin doesn't care if all branches are handled in a when statement. However, if you
 * use the when statement as an expression (with a value) it will force all cases to be handled.
 *
 * This helper is to make a lightweight way to say you meant to match all of them.
 *
 * Usage:
 *
 * ```
 * when(sealedObject) {
 *     is OneType -> //
 *     is AnotherType -> //
 * }.checkAllMatched
 */
val <T> T.checkAllMatched: T
    get() = this

// region UI utils

/**
 * Retrieves a color from the theme by attributes. If the attribute is not defined, a fall back
 * color will be returned.
 */
@ColorInt
fun Context.getThemeColor(
    @AttrRes attrResId: Int,
    @ColorRes fallbackColorResId: Int
): Int {
    val tv = TypedValue()
    return if (theme.resolveAttribute(attrResId, tv, true)) {
        tv.data
    } else {
        ContextCompat.getColor(this, fallbackColorResId)
    }
}

fun safeText(str: String?) : String = str ?: ""

fun <F> AppCompatActivity.replaceFragment(fragment: F, layoutId: Int) where F : Fragment {
    supportFragmentManager.inTransaction {
        replace(layoutId, fragment)
    }
}
// endregion

fun <T> Fragment.startPlainActivity(target: Class<T>) where T : AppCompatActivity {
    startActivity(Intent(context, target))
}

fun <T> AppCompatActivity.startPlainActivity(target: Class<T>) where T : AppCompatActivity {
    startActivity(Intent(this, target))
}

fun <T> AppCompatActivity.startPlainActivityForResult(target: Class<T>, requestCode: Int) where T : AppCompatActivity {
    startActivityForResult(Intent(this, target), requestCode)
}

fun <T> AppCompatActivity.startActivityWithData(target: Class<T>, append: (intent: Intent) -> Unit) where T : AppCompatActivity {
    val intent = Intent(this, target)
    intent.also {
        append(it)
    }
    startActivity(intent)
}

fun <T> List<T>.toArrayList() : ArrayList<T> {
    val arr = ArrayList<T>()
    this.forEach { t ->
        arr.add(t)
    }
    return arr
}

fun Context.isPortrait() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
fun Context.isLandscape() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

fun View.assignText(@IdRes viewId: Int, text: String?) {
    this.findViewById<TextView>(viewId).text = text
}

fun Context.showToast(msg: String, gravity: Int? = null, x: Int = 0, y: Int = 0) {
    val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
    gravity?.also { toast.setGravity(it, x, y) }
    toast.show()
}

/**
 * 视图转成Bitmap
 *
 * @return
 */
fun View.toBitmap(): Bitmap {
    val v = this
    if (v.measuredHeight <= 0) {
        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val b = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        v.draw(c)
        return b
    } else {
        val b = Bitmap.createBitmap(
            v.measuredWidth,
            v.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }
}
