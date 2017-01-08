package com.yangc.utils.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeUtils {

	private SerializeUtils() {
	}

	public static byte[] serialize(Object obj) {
		if (obj != null) {
			ByteArrayOutputStream baos = null;
			ObjectOutputStream oos = null;
			try {
				baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				oos.flush();
				baos.flush();
				byte[] b = baos.toByteArray();
				oos.close();
				oos = null;
				baos.close();
				baos = null;
				return b;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (oos != null) oos.close();
					if (baos != null) baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static Object deserialize(byte[] b) {
		if (b != null) {
			ByteArrayInputStream bais = null;
			ObjectInputStream ois = null;
			try {
				bais = new ByteArrayInputStream(b);
				ois = new ObjectInputStream(bais);
				Object obj = ois.readObject();
				ois.close();
				ois = null;
				bais.close();
				bais = null;
				return obj;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if (ois != null) ois.close();
					if (bais != null) bais.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
