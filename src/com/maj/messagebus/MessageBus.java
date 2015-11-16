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
 * ��Ϣ͸����
 * 
 * ʵ�ֶ�ĳ�����������������û������µ���Ϣ���۲콫�������յ���Ϣ��
 * ͨ������ϵķ�ʽ��ʵ�ֶ���֮�����Ϣ͸��
 * 
 * ʹ�÷�ʽ�ܼ򵥣��ܹ���������
 * 1.�۲�����Ҫʵ��ISimpleObserver�ӿڡ�
 * 2.�۲�����Ҫ����registObserver(String topic, ISimpleObserver
 * observer)ע�����ע�Ļ��⡣��ʼ�Ի���ļ���
 * 3.����������յ��û������Ϣ������unReginstObserver(String topic, ISimpleObserver
 * observer)ע����ע
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
	 * ע���Ϊ�۲���
	 * 
	 * @param topic ϣ���۲�Ļ���
	 * @param observer �۲��߶���
	 */
	public void registObserver(String topic, IMessageBus observer) {
		if (mObserverMap.containsKey(topic)) { // �����ǰ����topic���Ѿ�ע����۲���
			List<WeakReference<IMessageBus>> weakObserverList = mObserverMap
			        .get(topic);
			if (weakObserverList == null) {
				weakObserverList = new ArrayList<WeakReference<IMessageBus>>();
			}
			
			Log.d(TAG, topic + "�� �۲��߸��� = " + weakObserverList.size());
			
			// ��ʶ�Ƿ�ע���
			boolean existObserver = hasRegisted(weakObserverList, observer) >= 0;
			
			if (!existObserver) {
				weakObserverList
				        .add(new WeakReference<MessageBus.IMessageBus>(
				                observer));
			}
		} else { // �����ǰ����topic��ûע����۲���
			List<WeakReference<IMessageBus>> valueList = new ArrayList<WeakReference<IMessageBus>>();
			valueList
			        .add(new WeakReference<MessageBus.IMessageBus>(
			                observer));
			mObserverMap.put(topic, valueList);
		}
	}
	
	/**
	 * ע����ĳ������Ĺ۲�
	 * 
	 * @param topic ֮ǰ���۲�Ļ���
	 * @param observer �۲��߶���
	 */
	public void unRegistObserver(String topic, IMessageBus observer) {
		if (mObserverMap.containsKey(topic)) { // �������ȷʵ����
			List<WeakReference<IMessageBus>> weakObserverList = mObserverMap
			        .get(topic);
			if (weakObserverList == null || weakObserverList.size() == 0) {
				mObserverMap.remove(topic);
				return;
			}
			
			Log.d(TAG, topic + "�� �۲��߸��� = " + weakObserverList.size());
			
			// �Ƿ�ע���
			int indexInList = hasRegisted(weakObserverList, observer);
			if (indexInList >= 0) { // ���ע���
				weakObserverList.remove(indexInList);
				
				if (weakObserverList.size() == 0) { // ע��֮���жϸ�topic�Ƿ��б�Ĺ۲���
					mObserverMap.remove(topic); // ���û�й۲��߹۲��topic�����map�����topic
				}
			}
		}
	}
	
	/**
	 * �㲥֪ͨ���й۲��˸�topic�Ĺ۲���
	 * 
	 * @param topic ����
	 * @param intent ��Ҫ�ش�������
	 */
	public void broadcastObserver(String topic, Intent intent) {
		if (mObserverMap.containsKey(topic)) {
			List<WeakReference<IMessageBus>> weakObserverList = mObserverMap
			        .get(topic);
			if (weakObserverList == null || weakObserverList.size() == 0) {
				return;
			}
			
			Log.d(TAG, topic + "�� �۲��߸��� = " + weakObserverList.size());
			
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
	 * �ж��Ƿ��Ѿ�ע���
	 * 
	 * @param weakObserverList �۲����б�
	 * @param observer �۲���
	 * @return ����observer��list�е����������û��ע����򷵻�-1�����򷵻�ֵ>=0
	 */
	private int hasRegisted(
	        List<WeakReference<IMessageBus>> weakObserverList,
	        IMessageBus observer) {
		int index = -1; // ���ע������򷵻�observer��list�е�����
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
		 * ������۲���������������֪ͨ�۲���
		 */
		void onReceivMessage(Intent intent);
	}
}

