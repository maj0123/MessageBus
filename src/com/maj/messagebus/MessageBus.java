/*
 * Copyright (C) 2015 maj, messageBus (https://github.com/maj0123/MessageBus)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.maj.messagebus;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.content.Intent;
import android.util.Log;

/**
 * 消息透传类
 * 
 * 实现对某个话题的侦听，如果该话题有新的消息，观察将立即接收到消息。
 * 通过松耦合的方式，实现对象之间的消息透传
 * 
 * 使用方式很简单，总共分三步：
 * 1.观察者需要实现ISimpleObserver接口。
 * 2.观察者需要调用registObserver(String topic, ISimpleObserver
 * observer)注册想关注的话题。开始对话题的监听
 * 3.如果不想再收到该话题的消息，调用unReginstObserver(String topic, ISimpleObserver
 * observer)注销关注
 * 
 * @author maj
 *
 */
public class MessageBus {
	
	private final String TAG = "SimpleObserverUtil";
	
	private static MessageBus mSimpleObserverUtil;
	
	public Map<String, List<WeakReference<IMessageBus>>> mObserverMap = new HashMap<String, List<WeakReference<MessageBus.IMessageBus>>>();
	
	private MessageBus() {
		
	}
	
	public static MessageBus instance() {
		if (mSimpleObserverUtil == null) {
			synchronized (MessageBus.class) {
				if (mSimpleObserverUtil == null) {
					mSimpleObserverUtil = new MessageBus();
				}
			}
		}
		
		return mSimpleObserverUtil;
	}
	
	/**
	 * 注册成为观察者
	 * 
	 * @param topic 希望观察的话题
	 * @param observer 观察者对象
	 */
	public void registObserver(String topic, IMessageBus observer) {
		if (mObserverMap.containsKey(topic)) { // 如果当前话题topic，已经注册过观察者
			List<WeakReference<IMessageBus>> weakObserverList = mObserverMap
			        .get(topic);
			if (weakObserverList == null) {
				weakObserverList = new ArrayList<WeakReference<IMessageBus>>();
			}
			
			Log.d(TAG, topic + "， 观察者个数 = " + weakObserverList.size());
			
			// 标识是否注册过
			boolean existObserver = hasRegisted(weakObserverList, observer) >= 0;
			
			if (!existObserver) {
				weakObserverList
				        .add(new WeakReference<MessageBus.IMessageBus>(
				                observer));
			}
		} else { // 如果当前话题topic还没注册过观察者
			List<WeakReference<IMessageBus>> valueList = new ArrayList<WeakReference<IMessageBus>>();
			valueList
			        .add(new WeakReference<MessageBus.IMessageBus>(
			                observer));
			mObserverMap.put(topic, valueList);
		}
	}
	
	/**
	 * 注销对某个话题的观察
	 * 
	 * @param topic 之前所观察的话题
	 * @param observer 观察者对象
	 */
	public void unRegistObserver(String topic, IMessageBus observer) {
		if (mObserverMap.containsKey(topic)) { // 如果话题确实存在
			List<WeakReference<IMessageBus>> weakObserverList = mObserverMap
			        .get(topic);
			if (weakObserverList == null || weakObserverList.size() == 0) {
				mObserverMap.remove(topic);
				return;
			}
			
			Log.d(TAG, topic + "， 观察者个数 = " + weakObserverList.size());
			
			// 是否注册过
			int indexInList = hasRegisted(weakObserverList, observer);
			if (indexInList >= 0) { // 如果注册过
				weakObserverList.remove(indexInList);
				
				if (weakObserverList.size() == 0) { // 注销之后判断该topic是否还有别的观察者
					mObserverMap.remove(topic); // 如果没有观察者观察该topic，则从map中清除topic
				}
			}
		}
	}
	
	/**
	 * 广播通知所有观察了该topic的观察者
	 * 
	 * @param topic 话题
	 * @param intent 需要回传的数据
	 */
	public void broadcastObserver(String topic, Intent intent) {
		if (mObserverMap.containsKey(topic)) {
			List<WeakReference<IMessageBus>> weakObserverList = mObserverMap
			        .get(topic);
			if (weakObserverList == null || weakObserverList.size() == 0) {
				return;
			}
			
			Log.d(TAG, topic + "， 观察者个数 = " + weakObserverList.size());
			
			for (int i = 0; i < weakObserverList.size(); i++) {
				WeakReference<IMessageBus> weakObserver = weakObserverList
				        .get(i);
				if (weakObserver != null) {
					IMessageBus observer = weakObserver.get();
					if (observer != null) {
						observer.onReceivMessage(intent);
					}
				}
			}
		}
	}
	
	/**
	 * 判断是否已经注册过
	 * 
	 * @param weakObserverList 观察者列表
	 * @param observer 观察者
	 * @return 返回observer在list中的索引，如果没有注册过则返回-1，否则返回值>=0
	 */
	private int hasRegisted(
	        List<WeakReference<IMessageBus>> weakObserverList,
	        IMessageBus observer) {
		int index = -1; // 如果注册过，则返回observer在list中的索引
		for (int i = 0; i < weakObserverList.size(); i++) {
			WeakReference<IMessageBus> weakObserver = weakObserverList
			        .get(i);
			if (weakObserver != null) {
				IMessageBus iSimpleObserver = weakObserver.get();
				if (iSimpleObserver == observer) {
					index = i;
					break;
				}
			}
		}
		
		return index;
	}
	
	public interface IMessageBus {
		
		/**
		 * 如果被观察者有所动作，则通知观察者
		 */
		void onReceivMessage(Intent intent);
	}
}

