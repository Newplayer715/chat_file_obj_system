package com.client_server;

import com.data.woss_data;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class server_thread {
    //����serversocket
    private ServerSocket server;
    //�ַ���ģʽ�ݴ��ַ�
    private StringBuilder str=new StringBuilder();
    //����ģʽ
    private ftp ftpinstance;
    //hashmap�洢�ͻ���
    private HashMap<String,Socket> allserverthreads=new HashMap<String, Socket>();
    //����������״̬
    //0���ַ���״̬
    //1��������״̬
    //3���ļ���״̬
    private int analyze_state=0;
//����������
    private boolean server_switch=true;
    private ArrayList<String> date_assembling=new ArrayList<String>();

    //������

    public ArrayList<String> getDate_assembling() {
        return date_assembling;
    }

    public void setDate_assembling(ArrayList<String> date_assembling) {
        this.date_assembling = date_assembling;
    }

    public int getAnalyze_state() {
        return analyze_state;
    }

    public void setAnalyze_state(int analyze_state) {
        this.analyze_state = analyze_state;
    }

    public HashMap<String, Socket> getAllserverthreads() {
        return allserverthreads;
    }

    public void setAllserverthreads(HashMap<String, Socket> allserverthreads) {
        this.allserverthreads = allserverthreads;
    }

    public ServerSocket getServer() {
        return server;
    }

    public void setServer(ServerSocket server) {
        this.server = server;
    }

    public StringBuilder getStr() {
        return str;
    }

    public void setStr(StringBuilder str) {
        this.str = str;
    }

    //������
    public server_thread(final int port) throws IOException {
        ftpinstance=ftp.getinstance();
        //������������ʶ
        //System.out.println("connected_server_checkbox   "+ftpinstance.getConnected_server_checkbox().isSelected());

        ftpinstance.getConnected_server_checkbox().setSelected(true);

        //  System.out.println("connected_server_checkbox   "+ftpinstance.getConnected_server_checkbox().isSelected());
        try {
            setServer(new ServerSocket(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //�����������׽����߳̿���
        new Thread(new Runnable(){

            public void run() {
                while (true) {
                    try {
                        //��ʼ����
                        Socket client = getServer().accept();
                        //���������߳�
                        int n=0;
                        while(allserverthreads.containsKey(client.getInetAddress().toString()+"@"+n))
                        {
                            n++;
                        }
                        Thread serverthread= new serverthread(client.getInetAddress().toString()+"@"+n,client);
                        serverthread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }}
        }).start();
    }
    //server��Ϣ��Ϣ����
    public  void analyze_massages_server(byte[] stream,String massage,String client_name,Socket client,OutputStream out,InputStream read)
    {
        System.out.println("����˿�ʼ������Ϣ");
        System.out.println("massage:"+massage);
        if(massage.startsWith("@obj:"))
        {
            //���������״̬
            setAnalyze_state(1);
            ftpinstance.getConnected_server_checkbox().setText("����ģʽ");
            return;
        }
        if(massage.startsWith("@file:"))
        {
            //�����ļ���״̬
            setAnalyze_state(2);
            ftpinstance.getConnected_server_checkbox().setText("�ļ�ģʽ");
            return;
        }
        if(massage.startsWith("@pwd:"))
        {
            System.out.println("��֤����");
            //��֤����

            String sub=massage.substring(5).trim();
            System.out.println("�ͻ�������:"+sub+":"+sub.length());
            System.out.println("���������:"+ftp.getinstance().getConnected_pwd_field().getText()+":"+ftp.getinstance(). getConnected_pwd_field().getText().length());
            if(sub.equals(ftp.getinstance().getConnected_pwd_field().getText()))
            {
                System.out.println("������ȷ");
                //�����̼߳�
                int n=0;
                while(allserverthreads.containsKey(client.getInetAddress().toString()+"@"+n))
                {
                    n++;
                }
                getAllserverthreads().put(client.getInetAddress().toString()+"@"+n,client);
                //������ȷ
                try {
                    ftp.getinstance().getServer_thread().senddata("@pwdcorrect:",client.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //���ӳɹ�
                ftp.getinstance().connect_successful_event();

                //�ı�����״̬
                ftp.getinstance().getConnected_link_checkbox().setSelected(true);
                ftp.getinstance().getConnected_link_checkbox().setText("����״̬:"+allserverthreads.size());
                ftp.getinstance().getConnect_connect_button().setEnabled(false);
                ftp.getinstance().getSendpage().setText("Ⱥ����Ϣ");
                //��ӡ��Ϣ
                ftp.getinstance().console_log_textarea_append(client.getInetAddress()+":���ӳɹ�");
                //ˢ��panel���
                ftp.getinstance().panel.updateUI();
            }
            else
            {
                System.out.println("�������");
                server_switch=false;


                //�������
                try {
                    ftp.getinstance().getServer_thread().senddata("@pwdwrong:",client.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //ֹͣ������߳�
                try {
                    out.close();
                    read.close();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        //�յ���Ϣ
        ftp.getinstance().console_log_textarea_append("�յ�"+client_name+"����Ϣ:"+massage);
    }

    //�����������������
    public void senddata(String massage,OutputStream out)
    {
        try {
            out.write(massage.trim().getBytes("gbk"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(massage.equals("@pwdwrong:"))
        ftp.getinstance().console_log_textarea_append("������󣬾ܾ�����");

    }

    //Ⱥ������
    public void senddata(String massage)
    {
        //Ⱥ��
        Iterator<Map.Entry<String, Socket>> iterator = allserverthreads.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Socket> entry = iterator.next();

            try {
                entry.getValue().getOutputStream().write(massage.trim().getBytes("gbk"));
                entry.getValue().getOutputStream().flush();
                ftp.getinstance().console_log_textarea_append("������Ϣ��"+entry.getKey()+":"+massage.trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("��������������");
    }
    //ֹͣ������
    public void stopserver()
    {
        for(Map.Entry<String,Socket> entry:allserverthreads.entrySet()) {
            try {
                entry.getValue().getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                entry.getValue().getOutputStream().flush();
                entry.getValue().getOutputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                entry.getValue().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            getServer().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //�ڲ���serverthread
    class serverthread extends Thread {
        //˽������
        private String name_client;
        private Socket client;
        private InputStream read;
        private OutputStream out;

        //������


        public String getName_client() {
            return name_client;
        }


        public void setName_client(String name) {
            this.name_client = name;
        }

        public InputStream getRead() {
            return read;
        }

        public void setRead(InputStream read) {
            this.read = read;
        }

        public OutputStream getOut() {
            return out;
        }

        public void setOut(OutputStream out) {
            this.out = out;
        }

        public Socket getClient() {
            return client;
        }

        public void setClient(Socket client) {
            this.client = client;
        }

        public serverthread(String name, Socket client) {
            setName_client(name);
            setClient(client);
        }

        //�̷߳���
        @Override
        public void run() {
            //��ӡ����
            System.out.println("�ͻ�������");

            //��ÿͻ��������
            try {
                out = new BufferedOutputStream(getClient().getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                read = new BufferedInputStream(getClient().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //ѭ����Ӧ
            server_switch=true;
            while (server_switch) {
                System.out.println("����״̬ѡ��ģʽ");
                //��Ϣģʽ
                if (getAnalyze_state() == 0) {
                    //�ݴ�������ֽ����鳤��
                    int len = 0;
                    byte[] buffer = new byte[1024];
                    try {

                        while ((len = read.read(buffer)) != -1) {
                            System.out.println("���ܵ���Ϣ");
                            getStr().delete(0, getStr().length());
                            getStr().append(new String(buffer, 0, buffer.length, "gbk").trim());

                            //ִ�в���
                            analyze_massages_server(buffer, getStr().toString(), getName_client(), getClient(),out,read);
                            buffer = new byte[1024];
                            if(getAnalyze_state()!=0||!server_switch)
                                break;

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //����ģʽ
                if (getAnalyze_state() == 1) {
                //����ģʽ����
                    System.out.println("����������ģʽ");
                    while (read!=null) {
                        //��������
                        //�Ƚ���gbk����ǰ60���ֽڣ�
                        byte[] protocol = new byte[60];
                        try {
                            getRead().read(protocol);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //����Э��
                        String pro = null;
                        try {
                            pro = new String(protocol, 0, protocol.length, "gbk");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        System.out.println("�ļ�Э��ͷ��"+pro);
                        if (pro.startsWith("@massage:")) {
                            //������Ϣģʽ
                            setAnalyze_state(0);
                            ftpinstance.getConnected_server_checkbox().setText("��Ϣģʽ");
                            break;
                        }
                        if (pro.startsWith("@file:")) {
                            //�����ļ�ģʽ
                            setAnalyze_state(2);
                            ftpinstance.getConnected_server_checkbox().setText("�ļ�ģʽ");
                            break;
                        }
                        if (pro.startsWith("@woss:")) {
                            //��ʼ�����ļ�  ��ӡ������־
                            ftp.getinstance().console_log_textarea_append("��ʼ����" + getName_client() + "��woss.log�ļ�");
                            ObjectInputStream objectInputStream = null;
//                            try {
//                                objectInputStream = new ObjectInputStream(read);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
                            woss_data obj = null;

                            //�Ƿ������
                            boolean complete=false;
                           while (!complete&&read!=null){
                            try {
                                int len=0;
                                byte[] buffer=new byte[200];
                                while ((len=read.read(buffer))!=-1){
//                                while ((obj = (woss_data) objectInputStream.readObject()) != null) {

                                objectInputStream=new ObjectInputStream(read);
                                    try {
                                        obj = (woss_data) objectInputStream.readObject();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    //�ж��Ƿ������
                                    if(obj.getUsername().equals("������ɶ���"))
                                    {
                                        complete=true;
                                        break;
                                    }

                                    //�õ�����
                                    String date = obj.getOnline();
                                    System.out.println("����ʱ�䣺" + date);
                                    Pattern pattern1 = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})\\s(\\d{1,2}):(\\d{1,2}):(\\d{1,2})");
                                    Matcher matcher1 = pattern1.matcher(date);
                                    matcher1.find();
                                    //���������մ����ļ��м��ļ���
                                    //�õ�����·��
                                    String save_path = ftp.getinstance().getConnected_savefile_filechooser().getText();
                                    String path = "";
                                    //��
                                    path = save_path + File.separator + "year" + matcher1.group(1);
                                    if (date_assembling.contains(matcher1.group(1))) {
                                        //������ڸ����ļ��У�����־
//                                        FileOutputStream outputStream = new FileOutputStream(new File(path + File.separator + obj.getUsername() + ".log"));
//                                        //��־��Ϣ
//                                        String log = "�û�����" + obj.getUsername() + "\n" + "����������" + obj.getProtocol_address() + "\n" + "����ʱ�䣺" + obj.getOnline() + "\n" + "����ʱ�䣺" + obj.getOnline_time() + "s" + "\n" + "�û�ip��" + obj.getIp() + "\n";
//                                        outputStream.write(log.getBytes());

                                    } else {
                                        //�����ļ���

                                        new File(path).mkdir();
                                        //���뼯��
                                        date_assembling.add(matcher1.group(1));
                                    }
                                    //��
                                    path = save_path + File.separator + "year" + matcher1.group(1) + File.separator + "mon" + matcher1.group(2);
                                    if (date_assembling.contains(matcher1.group(1) + "-" + matcher1.group(2))) {
                                        //������ڸ����ļ��У�����־
//                                        FileOutputStream outputStream = new FileOutputStream(new File(path + File.separator + obj.getUsername() + ".log"));
//                                        //��־��Ϣ
//                                        String log = "�û�����" + obj.getUsername() + "\n" + "����������" + obj.getProtocol_address() + "\n" + "����ʱ�䣺" + obj.getOnline() + "\n" + "����ʱ�䣺" + obj.getOnline_time() + "s" + "\n" + "�û�ip��" + obj.getIp() + "\n";
//                                        outputStream.write(log.getBytes());
                                    } else {
                                        //�����ļ���
                                        new File(path).mkdir();
                                        //���뼯��
                                        date_assembling.add(matcher1.group(1) + "-" + matcher1.group(2));
                                    }
                                    //��
                                    path = save_path + File.separator + "year" + matcher1.group(1) + File.separator + "mon" + matcher1.group(2) + File.separator + "day" + matcher1.group(3);
                                    if (date_assembling.contains(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3))) {
                                        //������ڸ����ļ��У�����־
//                                        FileOutputStream outputStream = new FileOutputStream(new File(path + File.separator + obj.getUsername() + ".log"));
//                                        //��־��Ϣ
//                                        String log = "�û�����" + obj.getUsername() + "\n" + "����������" + obj.getProtocol_address() + "\n" + "����ʱ�䣺" + obj.getOnline() + "\n" + "����ʱ�䣺" + obj.getOnline_time() + "s" + "\n" + "�û�ip��" + obj.getIp() + "\n";
//                                        outputStream.write(log.getBytes());
                                    } else {
                                        //�����ļ���
                                        new File(path).mkdir();
                                        //���뼯��
                                        date_assembling.add(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3));
                                    }
                                    //ʱ
                                    path = save_path + File.separator + "year" + matcher1.group(1) + File.separator + "mon" + matcher1.group(2) + File.separator + "day" + matcher1.group(3) + File.separator + "hour" + matcher1.group(4);
                                    if (date_assembling.contains(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3) + "_" + matcher1.group(4))) {
                                        //������ڸ�ʱ�ļ��У�����־
//                                        FileOutputStream outputStream = new FileOutputStream(new File(path + File.separator + obj.getUsername() + ".log"));
//                                        //��־��Ϣ
//                                        String log = "�û�����" + obj.getUsername() + "\n" + "����������" + obj.getProtocol_address() + "\n" + "����ʱ�䣺" + obj.getOnline() + "\n" + "����ʱ�䣺" + obj.getOnline_time() + "s" + "\n" + "�û�ip��" + obj.getIp() + "\n";
//                                        outputStream.write(log.getBytes());
                                    } else {
                                        //�����ļ���
                                        new File(path).mkdir();
                                        //���뼯��
                                        date_assembling.add(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3) + "_" + matcher1.group(4));
                                    }
                                    //��
                                    path = save_path + File.separator + "year" + matcher1.group(1) + File.separator + "mon" + matcher1.group(2) + File.separator + "day" + matcher1.group(3) + File.separator + "hour" + matcher1.group(4) + File.separator + "min" + matcher1.group(5);
                                    if (date_assembling.contains(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3) + "_" + matcher1.group(4) + ":" + matcher1.group(5))) {
                                        //������ڸ÷��ļ��У�����־

                                    } else {
                                        //�����ļ���
                                        new File(path).mkdir();

                                        //���뼯��
                                        date_assembling.add(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3) + "_" + matcher1.group(4) + ":" + matcher1.group(5));
                                    }
                                    //��
                                    path = save_path + File.separator + "year" + matcher1.group(1) + File.separator + "mon" + matcher1.group(2) + File.separator + "day" + matcher1.group(3) + File.separator + "hour" + matcher1.group(4) + File.separator + "min" + matcher1.group(5)+ File.separator + "sec" + matcher1.group(6);
                                    if (date_assembling.contains(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3) + "_" + matcher1.group(4) + ":" + matcher1.group(5)+":"+matcher1.group(6))) {
                                        //������ڸ÷��ļ��У�����־

                                    } else {
                                        //�����ļ���
                                        new File(path).mkdir();

                                        //���뼯��
                                        date_assembling.add(matcher1.group(1) + "-" + matcher1.group(2) + "-" + matcher1.group(3) + "_" + matcher1.group(4) + ":" + matcher1.group(5)+":"+matcher1.group(6));
                                    }
                                    FileOutputStream outputStream = new FileOutputStream(new File(path + File.separator + obj.getUsername() + ".log"));
                                    //��־��Ϣ
                                    String log = "�û�����" + obj.getUsername() + "\n" + "����������" + obj.getProtocol_address() + "\n" + "����ʱ�䣺" + obj.getOnline() + "\n" + "����ʱ�䣺" + obj.getOnline_time() + "s" + "\n" + "�û�ip��" + obj.getIp() + "\n";
                                    outputStream.write(log.getBytes());

                                }
                            } catch(StreamCorruptedException e){

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            catch (ClassNotFoundException e) {
//                                e.printStackTrace();
//                            }


                        }
                            //��ӡ�ļ��������
                            ftp.getinstance().console_log_textarea_append("�������");

                            //�����ļ�����
                            if(getAnalyze_state()!=1)
                                break;

                        }
                    }
                }
                //�ļ���ģʽ
                if (getAnalyze_state() == 2) {
                    System.out.println("�������ļ�ģʽ");
                    //�洢·��
                    String save_directory=ftp.getinstance().getConnected_savefile_filechooser().getText();
                    System.out.println("�洢·����"+save_directory);
                    while (read!=null) {
                        //��������
                        //�Ƚ���gbk����ǰ60���ֽڣ�
                        byte[] protocol = new byte[60];
                        try {
                            getRead().read(protocol);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //����Э��
                        String pro = null;
                        try {
                            pro = new String(protocol, 0, protocol.length, "gbk");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        System.out.println("�ļ�Э��ͷ��"+pro);
                        if (pro.startsWith("@massage:")) {
                            //������Ϣģʽ
                            setAnalyze_state(0);
                            ftpinstance.getConnected_server_checkbox().setText("��Ϣģʽ");
                            break;
                        }
                        if (pro.startsWith("@obj:")) {
                            //�������ģʽ
                            setAnalyze_state(1);
                            ftpinstance.getConnected_server_checkbox().setText("����ģʽ");
                            break;
                        }
                        if (pro.startsWith("@file:")) {
                            System.out.println("��ʼ�и�Э���׺");
                            //�и���ļ���׺
                            String tail=pro.substring(6).trim();
                            System.out.println("tail:"+tail);
                            //�����ļ�·���Լ��ļ�������
                            FileOutputStream os=null;

                            try {
                                os=new FileOutputStream(new File(ftp.getinstance().getConnected_savefile_filechooser().getText()+tail));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            System.out.println("����·����"+ftp.getinstance().getConnected_savefile_filechooser().getText()+tail);
                            //��ʼ�����ļ�  ��ӡ������־
                            ftp.getinstance().console_log_textarea_append("��ʼ����"+getName_client()+"���ļ�");


                            byte[] buffer = new byte[1024];
                            int len =0;

                            try {
                                while ((len=read.read(buffer))!=-1)
                                {
                                    System.out.println("�յ��ļ����ݲ���");
                                    os.write(buffer, 0, len);    //д��ָ���ط�
                                    System.out.println("�յ����ȣ�"+len);
                                    if(len==1024)
                                    continue;
                                    else
                                        break;
                                }
                            }catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            //��ӡ�ļ��������
                            ftp.getinstance().console_log_textarea_append("�������");

                            //�����ļ�����
                            if(getAnalyze_state()!=2)
                            break;

                        }
                    }
                }
            }
        }
    }
}



