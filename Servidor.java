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
            String path = "";
            path = "Sincronizada";
            int tam = path.length();
            System.out.println("\n La direccion es: " + path);
                buscaArchivo(path,cl);
                enviaRutas(cl,tam);
                corto.clear();
                path_ar.clear();
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