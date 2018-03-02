import java.net.*;
import java.io.*;

public class Recibe{
    public static void main(String[] args) {
        try{
            int pto = 7000;
            ServerSocket s = new ServerSocket(pto);
            System.out.println("Servidor iniciado, esperando archivos...");
            for(;;){
                Socket cl = s.accept();
                DataInputStream dis = new DataInputStream(cl.getInputStream());
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
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}