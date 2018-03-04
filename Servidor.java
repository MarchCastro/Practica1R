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
            
        
                //Menu para agregar o eliminar archivo
                //NUEVO PUERTO
                ServerSocket s3 = new ServerSocket(9001);
                String opc = "";
                BufferedReader br2 = new BufferedReader(new InputStreamReader(cl.getInputStream()));;
                while(true){
                    System.out.println("Regreso while");
                    opc = br2.readLine();
                    System.out.println(" Opcion elegida: " + opc);
     
                    if(opc.compareTo("1") == 0){
                        System.out.println("Comienza recepción individual");
                        //Recibo el número de archivos
                        opc = br2.readLine();
                        System.out.println(" Recibiré: " + opc);
                        int numero_acv = Integer.parseInt(opc);
                        creaCarpeta(numero_acv,cl,s3);
                    }else if(opc.compareTo("2") == 0){
                        System.out.println(" Eliminar archivos ");
                        String path_elimina = br2.readLine();
                        System.out.println(" Ruta recibida " + path_elimina);
                        eliminarCarpeta(cl, path_elimina);
                    }else{
                        System.out.println("\n No recibí una opción :( ");
                    }
                }
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void creaCarpeta(int numero_archivos, Socket cl, ServerSocket s2){
        try {
            //BufferedReader br3 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            String path = "";
            String path1 = "/home/marce/Documents/Redes/P1/Practica1R/Prueba/";
            String path_completa = "";
            DataInputStream dis = null;
            DataOutputStream dos = null;
            
            
            for(int i = 0; i < numero_archivos; i++){
                //Recibo el archivo
                Socket cl2 =  s2.accept();
                dis = new DataInputStream(cl2.getInputStream());
                path = dis.readUTF();
                //Carpeta creada de prueba
                String prueba = "Hola/";
                path_completa = path1+prueba+path;
                
                //Creo variable con el path y creo carpetas
                File f = new File(path_completa);
                f.getParentFile().mkdirs(); 
                f.createNewFile();

                System.out.println("Path guarda: " + path_completa);
                    long tam1 = dis.readLong();
                    System.out.println("Inicia recepción del archivo: " + path_completa + " de tamaño " + tam1 + " desde " + cl.getInetAddress() + ":" + cl.getPort());
                    dos = new DataOutputStream(new FileOutputStream(path_completa));
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
            System.out.println("\nTermina de recibir");   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void eliminarCarpeta(Socket cl, String path){
        try {
            System.out.println("Entre a eliminar la carpeta" + path);
            File directorio = new File(path);
            //Llamo a borrar las carpetas
            borrarCarpeta(directorio);
            //Verifico si se elimino
            if(directorio.delete())
                System.out.println("El directorio " + path + " ha sido borrado correctamente");   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void borrarCarpeta(File directorio){
        System.out.println("Borrar"+directorio);
        if (directorio.exists()){
            if(directorio.isFile()){
                directorio.delete();
                System.out.println("El directorio " + directorio + " ha sido borrado correctamente");   
            }else{
                File[] f = directorio.listFiles();
                for (int x=0; x < f.length; x++){
                    System.out.println("f[x]: "+f[x]);
                    if(f[x].isDirectory()){
                        borrarCarpeta(f[x]);
                    }
                    f[x].delete();
                }
            }
        }else{
            System.out.println("El directorio no existe");
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