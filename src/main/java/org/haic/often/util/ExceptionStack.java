package org.haic.often.util;

import java.util.HashSet;

public class ExceptionStack {

	public static String getExceptionStack(Throwable e) {
		return getExceptionStack(e, new HashSet<>(), 0);
	}

	private static String getExceptionStack(Throwable e, HashSet<String> set, int num) {
		var stackTraceElements = e.getStackTrace();
		var prefix = num == 0 ? "Exception in thread " + "\"" + Thread.currentThread().getName() + "\" " : "Caused by: ";
		StringBuilder result = new StringBuilder().append(prefix).append(e).append("\n");
		int lenth = stackTraceElements.length - 1;
		for (int i = 0; i <= lenth; i++) {
			var err = stackTraceElements[i].getClassName() + "." + stackTraceElements[i].getMethodName() + "(" + stackTraceElements[i].getFileName() + "." + stackTraceElements[i].getLineNumber() + ")";
			if (set.contains(err)) {
				continue;
			}
			set.add(err);
			result.append("\tat ").append(err).append("\n");
		}

		var t = e.getCause();
		var cause = "";
		if (t != null) {
			num++;
			cause = getExceptionStack(t, set, num);
		}
		return result + cause;
	}

}
