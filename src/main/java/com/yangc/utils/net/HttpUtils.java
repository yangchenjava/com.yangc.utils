package com.yangc.utils.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HttpUtils {

	private static final String UTF_8 = "UTF-8";

	private static final int TIMEOUT = 6000;

	private HttpUtils() {
	}

	/**
	 * @功能: 发送get请求
	 * @作者: yangc
	 * @创建日期: 2013-1-9 上午11:01:09
	 */
	public static String sendGet(String uri, Map<String, String> paramsMap) {
		String params = "";
		if (paramsMap != null && !paramsMap.isEmpty()) {
			for (Entry<String, String> entry : paramsMap.entrySet()) {
				params += "&" + entry.getKey() + "=" + entry.getValue();
			}
		}
		if (StringUtils.isNotBlank(params)) {
			uri += "?" + params.substring(1);
		}

		HttpURLConnection conn = null;
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(uri);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true); // 设置输入流采用字节流
			conn.setDoInput(true); // 设置输出流采用字节流
			conn.setUseCaches(false);
			conn.setRequestProperty("Charset", UTF_8);
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			conn.connect();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF_8));
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str.trim());
			}
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return null;
	}

	/**
	 * @功能: 发送post请求
	 * @作者: yangc
	 * @创建日期: 2013-1-9 上午11:31:24
	 */
	public static String sendPost(String uri, Map<String, String> paramsMap) {
		String params = "";
		if (paramsMap != null && !paramsMap.isEmpty()) {
			for (Entry<String, String> entry : paramsMap.entrySet()) {
				params += "&" + entry.getKey() + "=" + entry.getValue();
			}
		}

		HttpURLConnection conn = null;
		BufferedReader br = null;
		DataOutputStream dos = null;
		StringBuilder sb = new StringBuilder();
		URL url;
		try {
			url = new URL(uri);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true); // 设置输入流采用字节流
			conn.setDoInput(true); // 设置输出流采用字节流
			conn.setUseCaches(false);
			conn.setRequestProperty("Charset", UTF_8);
			conn.setConnectTimeout(TIMEOUT);
			conn.setReadTimeout(TIMEOUT);
			conn.setRequestMethod("POST");
			if (StringUtils.isNotBlank(params)) {
				dos = new DataOutputStream(conn.getOutputStream());
				dos.write(params.substring(1).getBytes(UTF_8));
				dos.flush();
			}
			conn.connect();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}
			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF_8));
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str.trim());
			}
			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (dos != null) {
					dos.close();
					dos = null;
				}
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (conn != null) {
				conn.disconnect();
				conn = null;
			}
		}
		return null;
	}

	/**
	 * @功能: 发送Apache get请求
	 * @作者: yangc
	 * @创建日期: 2013-1-9 上午11:01:09
	 */
	public static String sendApacheGet(String uri, Map<String, String> paramsMap) {
		String params = null;
		if (paramsMap != null && !paramsMap.isEmpty()) {
			List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
			for (Entry<String, String> entry : paramsMap.entrySet()) {
				paramsList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			params = URLEncodedUtils.format(paramsList, UTF_8);
		}
		if (StringUtils.isNotBlank(params)) {
			uri += "?" + params;
		}

		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(uri);
		httpGet.setConfig(RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build());
		BufferedReader br = null;
		try {
			HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String str = null;
				StringBuilder sb = new StringBuilder();
				br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), UTF_8));
				while ((str = br.readLine()) != null) {
					sb.append(str.trim());
				}
				return sb.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 发送Apache post请求
	 * @作者: yangc
	 * @创建日期: 2013-1-9 上午11:31:24
	 */
	public static String sendApachePost(String uri, Map<String, Object> paramsMap) {
		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(uri);
		httpPost.setConfig(RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build());

		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		if (paramsMap != null && !paramsMap.isEmpty()) {
			for (Entry<String, Object> entry : paramsMap.entrySet()) {
				if (entry.getValue() instanceof File) {
					multipartEntityBuilder.addBinaryBody(entry.getKey(), (File) entry.getValue());
				} else {
					multipartEntityBuilder.addTextBody(entry.getKey(), entry.getValue().toString(), ContentType.create("text/plain", UTF_8));
				}
			}
		}
		httpPost.setEntity(multipartEntityBuilder.build());
		BufferedReader br = null;
		try {
			HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String str = null;
				StringBuilder sb = new StringBuilder();
				br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), UTF_8));
				while ((str = br.readLine()) != null) {
					sb.append(str.trim());
				}
				return sb.toString();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		// String uri = "http://119.253.49.102:9080/interface/getUserListByGroupIdAndNickname.do";
		// Map<String, String> paramsMap = new HashMap<String, String>();
		// paramsMap.put("groupId", "0");
		//
		// System.out.println(HttpUtils.sendGet(uri, paramsMap));
		// System.out.println(HttpUtils.sendPost(uri, paramsMap));
		// System.out.println(HttpUtils.sendApacheGet(uri, paramsMap));
		// System.out.println(HttpUtils.sendApachePost(uri, paramsMap));

		CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("http://127.0.0.1:81/drvrec-uc-ws/ws/0.1/user/login");
		httpPost.setConfig(RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).build());
		httpPost.setEntity(new StringEntity("{\"mobile\":\"13718922561\", \"password\":\"123456\", \"imei\":\"ddd\"}", UTF_8));
		httpPost.setHeader("Content-Type", "application/json; Charset=UTF-8");
		httpPost.setHeader("Cookie", "SSOcookie=7ec87bd3-ca72-46b0-b23c-0b681fd55791");
		BufferedReader br = null;
		try {
			HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String str = null;
				StringBuilder sb = new StringBuilder();
				br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), UTF_8));
				while ((str = br.readLine()) != null) {
					sb.append(str.trim());
				}
				System.out.println(sb.toString());
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
