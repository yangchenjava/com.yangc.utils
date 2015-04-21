package com.yangc.utils.lang;

public class GPSUtils {

	/** 地球半径（单位：公里） */
	public final static double EARTH_RADIUS_KM = 6378.137;

	/**
	 * @功能: 根据经纬度计算地球上任意两点间的距离
	 * @作者: yangc
	 * @创建日期: 2015年4月21日 下午11:19:01
	 * @param lat1 起点纬度
	 * @param lng1 起点经度
	 * @param lat2 终点纬度
	 * @param lng2 终点经度
	 * @return 两点距离（单位：千米）
	 */
	public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
		double radLat1 = Math.toRadians(lat1);
		double radLat2 = Math.toRadians(lat2);

		double radLng1 = Math.toRadians(lng1);
		double radLng2 = Math.toRadians(lng2);

		double deltaLat = radLat1 - radLat2;
		double deltaLng = radLng1 - radLng2;

		double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(deltaLng / 2), 2)));
		return NumberUtils.round(distance * EARTH_RADIUS_KM, 4);
	}

	public static void main(String[] args) {
		System.out.println(GPSUtils.getDistance(41.797944, 123.439618, 41.790522, 123.442708));
	}

}
