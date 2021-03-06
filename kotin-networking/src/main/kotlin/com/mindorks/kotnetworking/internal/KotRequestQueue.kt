/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mindorks.kotnetworking.internal

import com.mindorks.kotnetworking.core.Core
import com.mindorks.kotnetworking.request.KotRequest
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by amitshekhar on 30/04/17.
 */
class KotRequestQueue private constructor() {

    val sequenceGenerator: AtomicInteger = AtomicInteger()
    var currentRequest: Set<KotRequest> = mutableSetOf()

    private object Holder {
        val INSTANCE = KotRequestQueue()
    }

    companion object {
        val instance: KotRequestQueue by lazy { Holder.INSTANCE }
    }

    fun addRequest(request: KotRequest) {
        synchronized(currentRequest) {
            try {
                currentRequest.plus(request)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        try {
            request.sequenceNumber = getSequenceNumber()
            var future = Core.instance
                    .executorSupplier
                    .forNetworkTasks()
                    .submit(KotRunnable(request))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getSequenceNumber(): Int = sequenceGenerator.incrementAndGet()

    fun finish(kotRequest: KotRequest) {
        synchronized(currentRequest) {
            currentRequest.minus(kotRequest)
        }
    }

}