package whisper.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用户保存用户聊天记录
 * 1.用户登陆时创建用户聊天记录文件【records/用户名/时间.db】
 * Created by kingwan on 2019/12/30.
 */
public class SaveFileUtil {
    //登陆时---读取文件
    //从数据库中读取最新的时间文件
    private void loadRecordFile(String name){
        File file = new File("records/"+name+"/"+getTimer()+".db");

    }


    //退出时---创建、保存文件
    String path;//文件路径
    private void  createRecordFile(String name){
        File file = new File("records/"+name+"/"+getTimer()+".db");
        if (!file.exists()){
            try {
                //文件不存在，创建文件
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("创建聊天文件失败");
            }
        }
    }

    //格式化时间
    private static String getTimer() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date());
    }
}
