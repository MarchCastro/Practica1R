import java.net.*;
import java.io.*;
import javax.swing.JFileChooser; //Caja de dialogo donde selecciono el archivo que quiero
import javax.swing.plaf.FileChooserUI;
//Investigr como implementar la funcionalidad Drag & Drop
public class Cliente{
    static JFileChooser jf;

    public static void Envia_datos(File f, Socket cl){
        try{
            //DataOutputStream dos = null;
            //DataInputStream dis = null;

            int porcentaje,n = 0;
            String path = f.getAbsolutePath();
            String nombre = f.getName();
            long tam = f.length();

            System.out.println("Conexion establecida ... iniciando envío del archivo" + nombre + "\n\n");
            
            long enviados = 0;
            byte [] b = new byte [1500];
            DataOutputStream dos = new DataOutputStream(cl.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(path));
            dos.writeUTF(nombre);
            dos.flush();
            dos.writeLong(tam);
            dos.flush();

            while(enviados < tam){
                n = dis.read(b);
                dos.write(b,0,n);
                enviados += n;
                porcentaje = (int)((enviados * 100)/tam);
                System.out.println("\r Se ha enviado el " + porcentaje + "% del archivo");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String [] args){
        try{
            int pto = 9000;
            BufferedReader br1 = new BufferedReader(new InputStreamReader(System.in));
            //System.out.print("\n Escribe la dirección del servidor: ");
            String dir = "127.0.0.1";
            //System.out.print("\n Escribe la dirección de la carpeta: ");
            String path = "/home/march/Music";

            Socket cl = new Socket(dir, pto);
            //Envio path
            PrintWriter opc = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
            opc.println(path);
            opc.flush();

            //Recibo numero de archivos
            BufferedReader br3 = new BufferedReader(new InputStreamReader(cl.getInputStream()));
            String numero = "";
            numero = br3.readLine();
            int numero1 = Integer.parseInt(numero);
            System.out.println("\n Hay: " + numero1);

            
            String nombre = "";
            for(int i = 0; i < numero1; i++){
                nombre = br3.readLine();
                System.out.println(nombre);
            }

            
            //Envia archivos          
            jf = new JFileChooser();
            jf.setMultiSelectionEnabled(true);
            
            int r = jf.showOpenDialog(null);
            int n = 0;
            if(n == JFileChooser.APPROVE_OPTION){
                
                File [] f = jf.getSelectedFiles();
                //Envio path
                System.out.println("Enviando numero" );
                opc.println(f.length);
                opc.flush();
                for(int i = 0; i < f.length; i++){
                    //cl = new Socket(dst,pto);
                    Envia_datos(f[i], cl);
                }
                System.out.println("\n Se ha completado el envio");
                cl.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}