package clientedropbox;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class Cliente extends javax.swing.JFrame {
    
    private Socket cl;
    private DataOutputStream dos;
    private DataInputStream dis;
    private BufferedReader br;
    private PrintWriter pw;
    private JTree tree;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel treeModel;
    private String carpetaActual = "\\";
    private String archivoActual = "";
    private JFileChooser jfc;
    private String ruta;
    private LinkedList<File> locales = new LinkedList<File>();
    
    private void enviaArchivo(File f, boolean ext){
        try {
            Socket cl2 = new Socket("localhost" , 9001);
            DataOutputStream dos = new DataOutputStream(cl2.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(f.getAbsolutePath()));
            String nombre = f.getName();
            long tam = f.length();
            System.out.println("Conexión establecida... iniciando el envío del archivo " + nombre + "\n\n");
            long enviados = 0;
            int porcentaje = 0;
            int n = 0;
            byte[] b = new byte[1500];
            if(!ext)
                dos.writeUTF(getRutaRelativa(f));
            else
                dos.writeUTF(carpetaActual + "\\" + getRutaRelativaExt(f));
            dos.flush();
            dos.writeLong(tam);
            dos.flush();
            while(enviados < tam){
                n = dis.read(b);
                dos.write(b,0,n);
                enviados += n;
                porcentaje = (int) ((enviados * 100) / tam);
                System.out.print("\rSe ha enviado el " + porcentaje + "% del archivo");
            }
            System.out.println("\nSe ha completado el envío");
            dos.close();
            dis.close();
            cl2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private String getCarpetaActual(String path, DefaultMutableTreeNode node){
        carpetaActual = "\\";
        if(!node.isRoot()){
            File auxF = new File("");
            if(node.getLevel() == 1){
                auxF = new File(path + "\\" + node.toString());
                archivoActual = auxF.getAbsolutePath();
                if(auxF.isDirectory()){
                    carpetaActual = "\\" + node.toString();
                }else{
                    carpetaActual = "\\";
                }
            }else{
                DefaultMutableTreeNode auxN = node;
                Stack<String> p = new Stack<String>();
                for(int i = 2; i <= node.getLevel(); i++){
                    auxN = (DefaultMutableTreeNode) auxN.getParent();
                    p.push(auxN.toString());
                }
                while(!p.isEmpty()){
                    carpetaActual += "\\" + p.pop();
                }
                auxF = new File(path + carpetaActual + "\\" + node.toString());
                archivoActual = auxF.getAbsolutePath();
                if(auxF.isDirectory())
                    carpetaActual += "\\" + node.toString();
                return carpetaActual;
            }
        }else
            archivoActual = path;
        return carpetaActual;
    }
    
    private File generaArbol(String path){
        File fileRoot = new File(path);
        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);
        tree.addTreeSelectionListener(new TreeSelectionListener(){
                public void valueChanged(TreeSelectionEvent e){
                    try{
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        carpetaActual = getCarpetaActual(path, node);
                        System.out.println(archivoActual);
                        jLabel2.setText("Carpeta actual: " + path + carpetaActual);
                    }catch(NullPointerException ex){
                        System.out.println("Se dio clic en espacio vacío");
                    }
                }
            }
        );
        return fileRoot;
    }

    private void actualizaArbol(String nombre, boolean accion){
        TreePath tp;
        DefaultMutableTreeNode node;
        if(accion){
            String carpeta = (ruta + carpetaActual).split(Pattern.quote("\\"))[(ruta + carpetaActual).split(Pattern.quote("\\")).length - 1];
            tp = find(root, carpeta);
            node = (DefaultMutableTreeNode) tp.getLastPathComponent();
            node.add(new DefaultMutableTreeNode(nombre));
        }else{
            TreePath auxTP = find(root, nombre);
            System.out.println(carpetaActual);
            System.out.println(nombre);
            DefaultMutableTreeNode auxN = (DefaultMutableTreeNode) auxTP.getLastPathComponent();
            String carpeta = (ruta + carpetaActual).split(Pattern.quote("\\"))[(ruta + carpetaActual).split(Pattern.quote("\\")).length - 1];
            tp = find(root, carpeta);
            node = (DefaultMutableTreeNode) tp.getLastPathComponent();
            System.out.println("Se borra en: " + node.toString());
            System.out.println("Nodo a borrar: " + auxN.toString());
            node.remove(auxN);
        }
        DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
        dtm.reload();
        tree.expandPath(tp);
    }
    
    public Cliente(String dist, String pto, String path) throws IOException {
        ruta = path;
        cl = new Socket(dist, Integer.parseInt(pto));
        dos = new DataOutputStream(cl.getOutputStream());
        //dis = new DataInputStream(new FileInputStream(path));
        pw = new PrintWriter(new OutputStreamWriter(cl.getOutputStream()));
        br = new BufferedReader(new InputStreamReader(cl.getInputStream()));
        System.out.println("Conexión establecida");
        File fileRoot = generaArbol(path);
        initComponents();
        jLabel1.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        jLabel2.setText("Carpeta actual: " + path);
        CreateChildNodes ccn = new CreateChildNodes(fileRoot, root);
        new Thread(ccn).start();
        int archivosEnServidor = Integer.parseInt(br.readLine());
        System.out.println("Hay " + archivosEnServidor + " archivos en servidor");
        String[] rutasServidor = new String[archivosEnServidor];
        for(int i = 0; i < archivosEnServidor; i++){
            rutasServidor[i] = br.readLine();
        }
        actualizaCarpeta(rutasServidor);
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane(tree);
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jButton1.setText("Seleccionar Archivo");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Eliminar Archivo");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Arrastra un archivo aquí para subirlo");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel2.setText("Carpeta seleccionada: ");

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clientedropbox/logo.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(24, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jButton1)))
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addGap(20, 20, 20))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addGap(54, 54, 54)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(178, 178, 178))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2))
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private TreePath find(DefaultMutableTreeNode root, String s) {
    @SuppressWarnings("unchecked")
    Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
    while (e.hasMoreElements()) {
        DefaultMutableTreeNode node = e.nextElement();
        if (node.toString().equalsIgnoreCase(s)) {
            return new TreePath(node.getPath());
        }
    }
    return null;
}
    
    String carpetaExt = "";
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int r = jfc.showOpenDialog(null);
        if(r == jfc.APPROVE_OPTION){
            File f = jfc.getSelectedFile();
            carpetaExt = f.getName();
            System.out.println(f.getAbsolutePath());
            copiaArchivos(f, ruta + carpetaActual);
            actualizaArbol();
            pw.println("1");
            pw.flush();
            locales.clear();
            if(f.isDirectory()){
                for(File fx: f.listFiles()){
                    getTodosArchivos(fx);
                }
            }else{
                locales.add(f);
            }
            pw.println(locales.size());
            pw.flush();
            for (File fx : locales){
                try {
                    enviaArchivo(fx, true);
                } catch (Exception ex) {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if(!archivoActual.equals(ruta) && !archivoActual.equals("")){
            int conf = JOptionPane.showConfirmDialog(null, "¿Estás seguro que deseas eliminar el archivo " + archivoActual + "?", "Eliminar archivo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(conf == JOptionPane.YES_OPTION){
                File f = new File(archivoActual);
                String rutaR = getRutaRelativa(f);
                if(f.isDirectory()){
                    File[] files = f.listFiles();
                    vaciaCarpeta(f);
                    String[] auxS = carpetaActual.split(Pattern.quote("\\"));
                    carpetaActual = "";
                    for(int i = 0; i < auxS.length - 1; i++){
                        carpetaActual += "\\" + auxS[i];
                    }
                }
                if(f.delete()){
                    actualizaArbol(f.getName(), false);
                    pw.println("2");
                    pw.flush();
                    pw.println(rutaR);
                    pw.flush();
                }
                else
                    System.out.println("NO SE BORRÓ!");
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    private void vaciaCarpeta(File f) {
        File[] files = f.listFiles();
        for(File fi : files){
            if(fi.isDirectory())
                vaciaCarpeta(fi);
            fi.delete();
        }
    }

    private void actualizaCarpeta(String[] rutasServidor) {
        for(File f: new File(ruta).listFiles()){
            getTodosArchivos(f);
        }
        if(rutasServidor.length < 1){ 
            pw.println(locales.size());
            pw.flush();
            for (File f : locales){
                try {
                    enviaArchivo(f, false);
                } catch (Exception ex) {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else{
            LinkedList<String> aEnviar = new LinkedList<>();
            for(File f: locales){
                if(!estaEn(getRutaRelativa(f), rutasServidor))
                    aEnviar.add(getRutaRelativa(f));
            }
            pw.println(aEnviar.size());
            pw.flush();
            for(String aE : aEnviar) {
                File f = new File(ruta + "\\" + aE);
                try{
                    enviaArchivo(f, false);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        locales.clear();
    }

    private String getRutaRelativa(File f) {
        String r = f.getAbsolutePath();
        return r.substring(ruta.length() + 1);
    }
    
    private String getRutaRelativaExt(File f){
        int i = f.getAbsolutePath().indexOf(carpetaExt);
        System.out.println(f.getAbsolutePath().substring(i));
        return f.getAbsolutePath().substring(i);
    }

    private boolean estaEn(String rutaRelativa, String[] rutasServidor) {
        for(String r : rutasServidor){
            if(r.equals(rutaRelativa))
                return true;
        }
        return false;
    }
    
    private void getTodosArchivos(File f){
        if(f.isDirectory()){
            for(File auxF : f.listFiles())
                getTodosArchivos(auxF);
        }else{
            locales.add(f);
        }
    }
    
    Stack<File> pila = new Stack<File>();

    private void copiaArchivos(File f, String destino) {
        Path p = Paths.get(destino + "\\" + f.getName());
        
        try {
            if(f.isDirectory()){
                Files.createDirectory(p);
                File[] d = f.listFiles();
                for(File fd : d){
                    copiaArchivos(fd, destino + "\\" + f.getName());
                }   
            }else{
                Path from = Paths.get(f.getAbsolutePath());
                //Reemplazamos el fichero si ya existe
                CopyOption[] options = new CopyOption[]{
                  StandardCopyOption.REPLACE_EXISTING,
                  StandardCopyOption.COPY_ATTRIBUTES
                };
                Files.copy(from, p, options);
            }
            pila.push(f);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void actualizaArbol() {
        while(!pila.isEmpty()){
            File f = (File) pila.pop();
            actualizaArbol(f.getName(), true);
            if(f.isDirectory()){
                carpetaActual += "\\" + f.getName();
            }
        }
    }
}
