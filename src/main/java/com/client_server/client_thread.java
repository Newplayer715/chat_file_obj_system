package com.client_server;

import com.data.woss_data;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class client_thread {
    //����
    private Socket client;
    //���������
    private OutputStream write;
    private InputStream read;
    private static BufferedInputStream is;
    //���������ļ�
    private static File file=null;
    //�ֽ�������
    private static double num=0;
    //�ͻ���ֻ����Ϣģʽ���ַ����ݴ�
    private StringBuilder str=new StringBuilder();
    //��Ӧ��������Ϣ
    private Thread clientthread;
    //�ͻ��˷�����������״̬
    //0:��Ϣģʽ
    //1������ģʽ
    //2:�ļ���ģʽ
    private int send_state=0;
    //ת����λС��
    DecimalFormat decimalFormat=new DecimalFormat("#.00");
    //��hashmap ��������������
    HashMap<String, woss_data> wossdatas=new HashMap<String, woss_data>();
    //��������
    private int row_num;

    //��ֹ������ʱ����frame����һ���������߳�
    private  obj_thread obj_thread;

    //�ͻ��˿���
    private boolean client_switch=true;
    //������


    public client_thread.obj_thread getObj_thread() {
        return obj_thread;
    }

    public void setObj_thread(client_thread.obj_thread obj_thread) {
        this.obj_thread = obj_thread;
    }

    public int getSend_state() {
        return send_state;
    }

    public void setSend_state(int send_state) {
        this.send_state = send_state;
    }

    public Socket getClient() {
        return client;
    }

    public void setClient(Socket client) {
        this.client = client;
    }

    public StringBuilder getStr() {
        return str;
    }

    public void setStr(StringBuilder str) {
        this.str = str;
    }

    //������
    public client_thread(String ip, int port) {


        try {
            setClient(new Socket(ip,port));

        } catch (IOException e) {
                e.printStackTrace();
            System.out.println("���ӷ�����ʧ��");
        }

//        try
//        {
//            ftp.getinstance().getClient_socket() .connect(new InetSocketAddress(ip,port));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            write=getClient().getOutputStream();
            read=getClient().getInputStream();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        //���ܷ�������Ϣ
        clientthread=new Thread(
                new Runnable() {
                    public void run() {
                        int len=0;
                        byte[] buffer=new byte[1024];

                        System.out.println("�ͻ��˿�ʼ������Ϣ");

                        try {

                                while ((len = read.read(buffer)) != -1) {
                                    System.out.println("���յ���Ϣ");
                                    getStr().delete(0, getStr().length());
                                    getStr().append(new String(buffer, 0, buffer.length, "gbk").trim());
                                    buffer = new byte[1024];
                                    //ִ�в���
                                    analyze_massages_client(getStr().toString());
                                    if(!client_switch)
                                        break;
                                }

                        }catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }

                }
        );
        clientthread.start();
    }

    //��������
    public  void senddata(String str) throws IOException
    {

        //��Ϣģʽ
        if(getSend_state()==0) {
            write.write(str.trim().getBytes("gbk"));
            write.flush();
            ftp.getinstance().console_log_textarea_append("������Ϣ��"+str.trim());
            //�������"@obj:",�������ģʽ
            if (str.equals("@obj:")) {
                setSend_state(1);
                ftp.getinstance().console_log_textarea_append("�������ģʽ");
                return;
            }
            //�������"@file:",�����ļ�ģʽ
            if (str.equals("@file:")) {
                setSend_state(2);
                ftp.getinstance().console_log_textarea_append("�����ļ�ģʽ");
                return;
            }
        }
        //����ģʽ
        if(getSend_state()==1) {
            ftp.getinstance().console_log_textarea_append("������Ϣ��" + str.trim());
            //�������"@massage:",������Ϣģʽ
            if (str.equals("@massage:")) {
                //����60���ֽڵ�Э���ַ�
                byte[] protocol=new byte[60];
                protocol=str.getBytes("gbk");
                write.write(protocol);
                write.flush();

                setSend_state(1);
                ftp.getinstance().console_log_textarea_append("�������ģʽ");
                return;
            }
            //�������"@file:",�����ļ�ģʽ
            if (str.equals("@file:")) {
                //����60���ֽڵ�Э���ַ�
                byte[] protocol=new byte[60];
                protocol=str.getBytes("gbk");
                write.write(protocol);
                write.flush();

                setSend_state(2);
                ftp.getinstance().console_log_textarea_append("�����ļ�ģʽ");
                return;
            }
            //�������"@woss:",����wossģʽ
            if (str.startsWith("@woss:")) {
                //�����������߳�
             obj_thread=new obj_thread(str);
             obj_thread.start();

            }
        }
        //�ļ�ģʽ
        if(getSend_state()==2)
        {
            //������Ϣģʽ
            if(str.equals("@massage:"))
            {
                //����60���ֽڵ�Э���ַ�
                byte[] protocol=new byte[60];
                protocol=str.getBytes("gbk");
                write.write(protocol);
                write.flush();
                //�ı�״̬
                setSend_state(0);
                ftp.getinstance().console_log_textarea_append("������Ϣģʽ");
                return;
            }
            //�������ģʽ
            if(str.equals("@obj:"))
            {
                //����60���ֽڵ�Э���ַ�
                byte[] protocol=new byte[60];
                protocol=str.getBytes("gbk");
                write.write(protocol);
                write.flush();
                //�ı�״̬
                setSend_state(1);
                ftp.getinstance().console_log_textarea_append("�������ģʽ");
            return;
            }
            //�����ļ�
            StringTokenizer tokenizer=new StringTokenizer(str,";");
            while (tokenizer.hasMoreElements())
            {
                String filename=tokenizer.nextToken();
               file=new File(filename);
               final FileInputStream inputStream=new FileInputStream(file);
               is=new BufferedInputStream(inputStream);

                //Э���ַ�����,����ȥ
                byte[] protocol=new byte[60];
                //�������ļ���׺
                Pattern pattern=Pattern.compile("\\\\([^\\\\]+\\.\\S+)$");
                Matcher matcher=pattern.matcher(filename);
                if(matcher.find())
                protocol=("@file:"+matcher.group(1)).getBytes("gbk");
                write.write(protocol);
                write.flush();
                new Thread(new Runnable() {
                    public void run() {
                        //��ʼ����
                        byte[] buffer = new byte[1024];
                        int len=0;
                        //��ӡ��ʼ����
                        ftp.getinstance().console_log_textarea_append("��ʼ�����ļ���");

                        int start = (int)System.currentTimeMillis();
                        try {
                        while ((len = is.read(buffer)) != -1) {

                                write.write(buffer, 0, len);

                            num+=len;
                            ftp.getinstance().console_log_textarea_append("������:"+decimalFormat.format((num/file.length())*100)+"%");

                        }} catch (IOException e) {
                        e.printStackTrace();
                    }
                        //�����ˢ��
                        try {
                            write.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int end = (int)System.currentTimeMillis();
                        ftp.getinstance().console_log_textarea_append("���ͻ���ʱ�䣺" + (int)((end-start)/1000)+"s");

                        num=0;
                        //�ر��ļ���
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        }
    }

    //client��Ϣ��Ϣ����
    public void analyze_massages_client(String massage)
    {
        System.out.println("�ͻ��˿�ʼ������Ϣ");

        //�������
        if(massage.startsWith("@pwdwrong:"))
        {
            try {
                client_switch=false;
                close_client();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //��ӡ��Ϣ
            ftp.getinstance(). console_log_textarea_append(new Date()+":�������");
            return;
        }
        if(massage.startsWith("@pwdcorrect:"))
        {
            //���ӳɹ�
            ftp.getinstance().connect_successful_event();

            //�ı�����״̬
            ftp.getinstance().getConnected_link_checkbox().setSelected(true);
            ftp.getinstance().getConnect_connect_button().setEnabled(false);
            ftp.getinstance().getSendpage().setText(ftp.getinstance().getConnect_ip_field().getText());
            //��ӡ��Ϣ

            ftp.getinstance().console_log_textarea_append(":�ɹ����ӵ�������"+ftp.getinstance().getConnect_ip_field().getText());
            //ˢ��panel���
            ftp.getinstance().panel.updateUI();
            return;
        }
        //��ӡ��Ϣ

        ftp.getinstance().console_log_textarea_append("�յ�"+ftp.getinstance().getConnect_ip_field().getText()+"����Ϣ:"+massage);
    }
    //�رտͻ���
   public void close_client() throws IOException {
        write.flush();
        write.close();
        read.close();
       client.close();
    }

    //�ڲ��࣬�������߳�
    class obj_thread extends Thread
    {
        //���͵�����
        private String str;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        //���캯��
       public obj_thread(String massage)
       {
           //��ʼ������
          setStr(massage);

       }
        @Override
        public void run() {
            //����60���ֽڵ�Э���ַ�
            byte[] protocol=new byte[60];
            try {
                protocol="@woss:".getBytes("gbk");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                write.write(protocol);
                write.flush();
            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println("��ʼ�и�Э���׺");
            //�и���ļ���׺
            String tail=str.substring(6).trim();


            //����woss��־��Ϣ
            File resource = new File(tail);
            //��ȡ����
            LineNumberReader lnr = null;
            try {
                lnr = new LineNumberReader(new FileReader(resource));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                lnr.skip(Long.MAX_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int lineNo = lnr.getLineNumber() + 1;

            //��ӡ��־
            ftp.getinstance().console_log_textarea_append("��ʼ��" + tail + "�����ɶ��󡣡���");
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(new FileInputStream(resource), "gbk");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            BufferedReader buffered = new BufferedReader(reader);
            //�ݴ���
            String row = "";
            Pattern pattern = Pattern.compile("#(\\w{0,13})\\|(\\S{13})\\|(\\d{1})\\|(\\d{8,12})\\|(\\d+.\\d+.\\d+.\\d+)");
            try {
                while ((row = buffered.readLine()) != null) {


                    //��ʼ������
                    Matcher matcher = pattern.matcher(row);
                    //������
                    matcher.find();

                    woss_data obj = null;
                    obj = wossdatas.get(matcher.group(5));
                    if (obj != null && matcher.group(3).equals("8")) {
                        //�����û������ߣ�����������¼
                        obj.setOnline_time(Integer.valueOf(matcher.group(4)) - Integer.valueOf(obj.getOnline_time_stamp()));
                        //���Ͷ���

                        ObjectOutputStream objectOutputStream=new ObjectOutputStream(write);
                        objectOutputStream.writeObject(obj);
                        objectOutputStream.flush();

                        write.flush();

                        //���������ۼ�
                        row_num++;
                        ftp.getinstance().console_log_textarea_append("�������ȣ�"+new DecimalFormat("#.00").format(((double)row_num/lineNo)*100)+"%");

                        //�Ƴ���¼
                        System.out.println("�Ƴ���"+wossdatas.size());
                        wossdatas.remove(matcher.group(5));
                        continue;
                    }
                    if(matcher.group(3).equals("7")){
                        //����map
                        System.out.println("���룺"+wossdatas.size());
                        woss_data object = new woss_data(matcher.group(1), matcher.group(2), ftp.stampToDate(matcher.group(4)), matcher.group(5), matcher.group(4));
                        wossdatas.put(matcher.group(5), object);
                        //���������ۼ�
                        row_num++;
                        ftp.getinstance().console_log_textarea_append("�������ȣ�"+new DecimalFormat("#.00").format(((double)row_num/lineNo)*100)+"%");

                    }
                    //debug��������
//                    System.out.println("�������ȣ�"+new DecimalFormat("#.00").format(((double)row_num/lineNo)*100)+"%");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            //�������
            //���ʹ�����ɶ���
            woss_data complete_obj=new woss_data("������ɶ���","","","","");
            //���Ͷ���
            try {
                write.flush();

                ObjectOutputStream objectOutputStream=new ObjectOutputStream(write);
                objectOutputStream.writeObject(complete_obj);
                objectOutputStream.flush();
            }catch (IOException e){
                e.printStackTrace();
            }


            ftp.getinstance().console_log_textarea_append("����woss.log�ļ��ɹ����ѷ���������");

        }
    }
}


