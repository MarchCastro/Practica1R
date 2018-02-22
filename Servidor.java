import java.net.*;
import java.io.*;

public class Servidor{
    public static void main(String[] args) {

        try{
            ServerSocket s = new ServerSocket(9000);
            s.setReuseAddress(true);
            System.out.println("Servicio iniciado, esperando clientes");
            for(;;){
            //Conexion con cliente
            Socket cl = s.accept();
            System.out.println("Cliente conectado desde:"+cl.getInetAddress()+":"+cl.getPort());
                
            BufferedReader br3 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            String path = "";
            path = br3.readLine();
            System.out.println("\n La direccion es: " + path);
           
            File f = new File(path);
            System.out.println("\n File " + f);
            if(f.exists()){ 
                File[] carpetas = f.listFiles();
                //Envio numero de archivos
                PrintWriter numero = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                numero.println(carpetas.length);
                numero.flush();
                //Envio nombres
                for (int x = 0; x < carpetas.length; x++){
                    System.out.println(carpetas[x].getName());
                    //DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
                    numero.println(carpetas[x].getName());
                    numero.flush();
                   
                }
            }else{ 
                System.out.println("No existe la carpeta :(");
            }
        }
            
            
            /*
            for(;;){
                Socket cl = s.accept();
                DataIputStream dis = new DataInputStream(cl.getInputStream());
                String nombre = dis.readUTF();
                long tam = dis.readLong();
                System.out.println("Inicia recepción del archivo: " + nombre + " de tamaño " + tam + " desde " + cl.getInetAddress() + ":" + cl.getPort());
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(nombre));
                long recibidos = 0;
                int n,porcentaje = 0;
                
                while(recibidos < tam){
                    byte [] b = new byte[1500];
                    n = dis.read(b);
                    recibidos += n;
                    dos.write(b,0,n);
                    dos.flush();
                    porcentaje = (int)((recibidos * 100)/tam);
                    System.out.println("\r Recibido el " + porcentaje + "% del archivo");
                }

                System.out.println("Archivo recibido");
                dos.close();
                dis.close();
                cl.close();
            }*/
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}