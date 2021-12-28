/*
 * Copyright (c) 2021 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.home.room.detail.timeline.helper

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.content.ContextCompat
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import im.vector.app.R
import im.vector.app.core.glide.GlideApp
import im.vector.app.features.home.AvatarRenderer
import org.billcarsonfr.jsonviewer.Utils
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.util.toMatrixItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationPinProvider @Inject constructor(
        private val context: Context,
        private val session: Session,
        private val avatarRenderer: AvatarRenderer
) {
    private val cache = mutableMapOf<String, Drawable>()

    private val glideRequests by lazy {
        GlideApp.with(context)
    }

    fun create(userId: String, callback: (Drawable) -> Unit) {
        if (cache.contains(userId)) {
            callback(cache[userId]!!)
            return
        }

        session.getUser(userId)?.toMatrixItem()?.let {
            val size = Utils.dpToPx(44, context)
            avatarRenderer.render(glideRequests, it, object : CustomTarget<Drawable>(size, size) {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    val bgUserPin = ContextCompat.getDrawable(context, R.drawable.bg_map_user_pin)!!
                    val layerDrawable = LayerDrawable(arrayOf(bgUserPin, resource))
                    val horizontalInset = Utils.dpToPx(4, context)
                    val topInset = Utils.dpToPx(4, context)
                    val bottomInset = Utils.dpToPx(8, context)
                    layerDrawable.setLayerInset(1, horizontalInset, topInset, horizontalInset, bottomInset)

                    cache[userId] = layerDrawable

                    callback(layerDrawable)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Is it possible? Put placeholder instead?
                }
            })
        }
    }
}