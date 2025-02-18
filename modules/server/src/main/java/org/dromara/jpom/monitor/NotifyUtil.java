/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Code Technology Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.dromara.jpom.monitor;

import cn.hutool.core.map.SafeConcurrentHashMap;
import org.dromara.jpom.model.BaseEnum;
import org.dromara.jpom.model.data.MonitorModel;

import java.util.Map;
import java.util.Objects;

/**
 * 通知util
 *
 * @author bwcx_jzy
 * @since 2019/7/13
 */
public class NotifyUtil {

	private static final Map<MonitorModel.NotifyType, INotify> NOTIFY_MAP = new SafeConcurrentHashMap<>();

	static {
		NOTIFY_MAP.put(MonitorModel.NotifyType.dingding, new WebHookUtil());
		NOTIFY_MAP.put(MonitorModel.NotifyType.mail, new EmailUtil());
		NOTIFY_MAP.put(MonitorModel.NotifyType.workWx, new WebHookUtil());
	}

	/**
	 * 发送报警消息
	 *
	 * @param notify  通知方式
	 * @param title   描述
	 * @param context 内容
	 */
	public static void send(MonitorModel.Notify notify, String title, String context) throws Exception {
		int style = notify.getStyle();
		MonitorModel.NotifyType notifyType = BaseEnum.getEnum(MonitorModel.NotifyType.class, style);
		Objects.requireNonNull(notifyType);
		//
		INotify iNotify = NOTIFY_MAP.get(notifyType);
		Objects.requireNonNull(iNotify);
		iNotify.send(notify, title, context);
	}

}
