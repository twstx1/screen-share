package com.codepath.examples.audiovideodemo;

//调用的代码
//int result = httpDownloader.downloadFile(mp3Url,"mp3/",mp3Info.getMp3Name());
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
 
import android.os.Environment;
 
//import com.example.domain.Mp3Info;
 
public class FileUtils {
	private static String SDCardRoot = Environment
			.getExternalStorageDirectory().getAbsolutePath() + File.separator;

	public FileUtils() {
		// SDCardRoot结果为：/mnt/sdcard/

	}

	// 在SD卡上创建目录：
	public static File createSDDir(String dir) {
		File dirFile = new File(SDCardRoot + dir + File.separator);
		System.out.println(dirFile.mkdirs()); // false
		return dirFile;
	}

	// 在SD卡上创建文件"
	public static File createFileInSDCard(String dir, String fileName)
			throws IOException {
		File file = new File(SDCardRoot + dir + File.separator + fileName);
		System.out.println("file......" + file);// 打印结果为"/mnt/sdcard/"</span>
		file.createNewFile();
		return file;
	}

	// 判断SD卡上的文件夹是否存在
	public static boolean isFileExist(String path, String fileName) {
		File file = new File(SDCardRoot + path + File.separator + fileName);
		return file.exists();
	}

	// 将一个InputStream里面的数据写到SD卡中
	public static File write2SDFromInputStream(String path, String fileName,
			InputStream in) {
		File file = null;
		OutputStream out = null;

		createSDDir(path);
		try {
			file = createFileInSDCard(path, fileName);
		} catch (IOException e) {
			System.out.println("文件创建失败");
		}

		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			System.out.println("文件没有找到");
		}
		byte buf[] = new byte[1024 * 4];
		int len = 0;
		try {
			while ((len = in.read(buf)) != -1) {
				out.write(buf, 0, len);
				out.flush();
			}
		} catch (IOException e) {
			System.out.println("SD写入失败！");
		} finally {

			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("SD写入流关闭失败！");
				}
		}

		return file;
	}
}