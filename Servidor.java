import java.net.*;
import java.io.*;
import java.util.*;

public class Servidor{
    static ArrayList<String> corto = new ArrayList<String>();
    static ArrayList<String> path_ar = new ArrayList<String>();
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
            DataInputStream dis = null;
            DataOutputStream dos = null;

            String path = "";
            path = "Sincronizada";
            int tam = path.length();
            System.out.println("\n La direccion es: " + path);
            buscaArchivo(path,cl);
            enviaRutas(cl,tam);
            //Recibo numero de archivo
            //BufferedReader br3 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            String numero = "";
            numero = br3.readLine();
            int numero1 = Integer.parseInt(numero);
            System.out.println("\n Recibiré: " + numero1);
            int pto2 = 9001;
            ServerSocket s2 = new ServerSocket(pto2);
            for(int i = 0; i < numero1; i++){
                 Socket cl2=  s2.accept();
                 dis = new DataInputStream(cl2.getInputStream());
                 String nombre1 = dis.readUTF();
                 System.out.println("nombre1:" + nombre1);
                 long tam1 = dis.readLong();
                 System.out.println("Inicia recepción del archivo: " + nombre1 + " de tamaño " + tam1 + " desde " + cl.getInetAddress() + ":" + cl.getPort());
                 dos = new DataOutputStream(new FileOutputStream("Sincronizada/"+ nombre1));
                 long recibidos = 0;
                 int n,porcentaje = 0;
                 
                 while(recibidos < tam1){
                     byte [] b = new byte[1500];
                     n = dis.read(b);
                     recibidos += n;
                     dos.write(b,0,n);
                     dos.flush();
                     porcentaje = (int)((recibidos * 100)/tam1);
                     System.out.print("\rRecibido el " + porcentaje + "% del archivo");
                 }
                 System.out.println("\nArchivo recibido");
                 dis.close();
                 dos.close();
                 cl2.close();
                 
            }
                s2.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

        public static void buscaArchivo(String path, Socket cl){
            try{
                //int tam_path = tam;
                File f = new File(path);

                if(f.exists()){ 
                    //Guarda en cada posicion un archivo
                    File[] carpetas = f.listFiles();
                    //Envio nombres
                    int i = 0;
                    for (int x = 0; x < carpetas.length; x++){
                        if (carpetas[x].isDirectory()){
                            path_ar.add(carpetas[x].getPath());
                            //System.out.println(carpetas[x].getName());
                            String path1 = carpetas[x].getPath();
                            buscaArchivo(path1, cl);                        
                        }                  
                        else{
                            //System.out.println(carpetas[x].getName());
                            String concatena = path + "\\" + carpetas[x].getName();
                            path_ar.add(x,concatena);
                            
                        }               
                    }                    
                }else{ 
                    System.out.println("No existe la carpeta :(");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        public static void enviaRutas(Socket cl, int tam){
            try{
                int tam_path = tam;
                for(int d = 0; d < path_ar.size(); d++){
                    //Acorto nombre
                    corto.add(path_ar.get(d).substring(tam_path+1, path_ar.get(d).length()));                       
                }
                //Envio nombres
                PrintWriter numero = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
                System.out.println("Tam corto: " + corto.size());
                numero.println(corto.size());
                numero.flush();
                //Envia rutas
                
                for(int d = 0; d < corto.size(); d++){
                    System.out.println("Nombre corto: " + corto.get(d));                    
                    numero.println(corto.get(d));
                    numero.flush();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

}