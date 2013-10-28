/*
 * Copyright (c) 2004-2013 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: Thanasis
 * Sep 17, 2013
 */
package convcao.com.caoAgent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Random;

import javax.swing.ImageIcon;

import org.apache.commons.net.ftp.FTPClient;

import pt.up.fe.dceg.neptus.NeptusLog;
import pt.up.fe.dceg.neptus.console.ConsoleLayout;
import pt.up.fe.dceg.neptus.imc.EstimatedState;
import pt.up.fe.dceg.neptus.imc.FollowRefState;
import pt.up.fe.dceg.neptus.imc.Reference;
import pt.up.fe.dceg.neptus.plugins.ConfigurationListener;
import pt.up.fe.dceg.neptus.plugins.NeptusProperty;
import pt.up.fe.dceg.neptus.plugins.PluginDescription;
import pt.up.fe.dceg.neptus.plugins.PluginDescription.CATEGORY;
import pt.up.fe.dceg.neptus.plugins.PluginUtils;
import pt.up.fe.dceg.neptus.plugins.Popup;
import pt.up.fe.dceg.neptus.plugins.SimpleSubPanel;
import pt.up.fe.dceg.neptus.plugins.controllers.ControllerManager;
import pt.up.fe.dceg.neptus.plugins.controllers.IController;
import pt.up.fe.dceg.neptus.renderer2d.Renderer2DPainter;
import pt.up.fe.dceg.neptus.renderer2d.StateRenderer2D;
import pt.up.fe.dceg.neptus.types.coord.LocationType;
import pt.up.fe.dceg.neptus.types.vehicle.VehicleType;
import pt.up.fe.dceg.neptus.types.vehicle.VehiclesHolder;
import pt.up.fe.dceg.neptus.util.GuiUtils;
import pt.up.fe.dceg.neptus.util.ImageUtils;
import pt.up.fe.dceg.neptus.util.comm.IMCUtils;
import pt.up.fe.dceg.neptus.util.comm.manager.imc.ImcMsgManager;

import com.google.gson.Gson;


/**
 * @author Thanasis
 *
 */
@PluginDescription(author="thanasis", category=CATEGORY.UNSORTED, name="convcao Neptus Interaction")
@Popup(accelerator=KeyEvent.VK_N, pos=Popup.POSITION.CENTER, height=500, width=510, name="convcao Neptus Interaction")
public class convcaoNeptusInteraction extends SimpleSubPanel implements Renderer2DPainter, IController, ConfigurationListener {

    private static final long serialVersionUID = -1330079540844029305L;
    
    // Variables declaration - do not modify
    protected int AUVS ;
    protected String SessionID = "";
    //protected double[][] MapAsTable;
    
    // 1 row per AUV, 1 column per coordinate
    //protected double[][] PosAUVS;
    
    
    protected boolean cancel = false;
    protected String Report = "";
    
    //backgroundWorker t1;
    
    //Thread updateThread = null;
    
    //GUI
    protected javax.swing.JButton jButton1;
    protected javax.swing.JButton jButton2;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JLabel jLabel4;
    protected javax.swing.JLabel jLabel5;
    protected javax.swing.JLabel jLabel7;
    protected javax.swing.JLabel jLabel8;
    protected javax.swing.JLabel jLabel9;
    protected javax.swing.JLabel jLabel10;
    protected javax.swing.JLabel jLabel11;
    protected javax.swing.JLabel jLabel12;
    protected javax.swing.JLabel jLabel6;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JPanel jPanel2;
    protected javax.swing.JPanel jPanelMain;
    protected javax.swing.JPasswordField jPasswordField1;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JTextArea jTextArea1;
    protected javax.swing.JTextField jTextField1;
    protected javax.swing.JTextPane jTextPane1;
    protected javax.swing.JButton renewButton;
    protected javax.swing.JButton connectButton;
    // End of variables declaration
    
    protected ImageIcon runIcon = ImageUtils.getIcon("images/checklists/run.png");
    protected ImageIcon appLogo = ImageUtils.getIcon("images/control-mode/externalApp.png");
    protected ImageIcon noptilusLogo = ImageUtils.getIcon("images/control-mode/noptilus.png");
    
    
    @NeptusProperty
    public String controlledVehicles = "lauv-noptilus-1,lauv-noptilus-2";
    
    @NeptusProperty
    public String controlledVehicleDepths = "1.0,2.5";  
    
    @NeptusProperty
    public int controlLatencySecs = 5;  
    
    @NeptusProperty
    public boolean useAcousticComms = false;
    
    public class InputData
    {
        public String DateTime = "";
        public String SessionID = "";
        public String DemoMode = "1";
        public String AUVs = "";
    };

    public class TransferData
    {
        public String SessionID = "";
        public int timeStep = 0;
        
        // 1 row per auv, 1 coordinates (depth)         
        public double[] Bathymeter;

        // 1 row per auv, 2 coordinates (northing, easting) 
        public double[][] Location;
    };
    
    int timestep = 1;
    
    protected void showText(String text) {
        jTextArea1.append(text+"\n");
        jTextArea1.repaint();
    }
    
    @Override
    public void propertiesChanged() {
        
    }
    
    protected NoptilusCoords coords = new NoptilusCoords();
    
    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        
        Point2D center = renderer.getScreenPosition(coords.squareCenter);
        double width = renderer.getZoom() * coords.cellWidth * coords.numCols;
        double height = renderer.getZoom() * coords.cellWidth * coords.numRows;
        g.setColor(new Color(0,255,0,128));
        g.fill(new Rectangle2D.Double(center.getX() - width/2, center.getY() - height / 2, width, height));        
    }
    
    public TransferData localState() {
        TransferData data = new TransferData();
        data.timeStep = this.timestep;
        data.SessionID = SessionID;
        data.Bathymeter = new double[AUVS];
        data.Location = new double[AUVS][3];
        
        for (int AUV = 0; AUV < AUVS; AUV++) {
            String auvName = nameTable.get(AUV);
            double noptDepth = coords.convertWgsDepthToNoptilusDepth(positions.get(auvName).getDepth());
            data.Bathymeter[AUV] = noptDepth-bathymetry.get(auvName);
            double[] nopCoords = coords.convert(positions.get(auvName));
            if (nopCoords == null) {
                GuiUtils.errorMessage(getConsole(), "ConvCAO", auvName+" is outside operating region");
                return null;
            }
                
            data.Location[AUV][0] = Math.round(nopCoords[0]);
            data.Location[AUV][1] = Math.round(nopCoords[1]);
            data.Location[AUV][2] = (int)noptDepth;                                   
        }
        
        return data;
    }
    
    /**
     * This method is called whenever the AUVs have reached their destinations and new data is to be sent to the server.<br/>
     * As a result, new destinations will be received and vehicles will travel to the new destinations.
     * @throws Exception If there is a problem communicating with the server.
     */
    public void controlLoop() throws Exception {
        
        TransferData send = localState();
        if (send == null)
            throw new Exception("Unable to compute local state");
        
        TransferData receive = new TransferData();
        
        for (int AUV = 0; AUV < AUVS; AUV++) {
            showText(nameTable.get(AUV)+" is at "+send.Location[AUV][0]+", "+send.Location[AUV][1]+", "+send.Location[AUV][2]);
        }
        
        Gson gson = new Gson();
        String json = gson.toJson(send);
        
        PrintWriter writer = null;
        writer = new PrintWriter(SessionID + "_Data.txt", "UTF-8");
        writer.write(json);
        writer.close();
        
        NeptusLog.pub().info("uploading to convcao..."+json.toString());
        Upload("www.convcao.com","NEPTUS","",jTextField1.getText(),new String(jPasswordField1.getPassword()),SessionID + "_Data.txt");
        
        showText("Upload complete, downloading new AUV destinations");
        
        int receivedTimestep = 0;
        while (!cancel && receivedTimestep < timestep) {            
            Thread.sleep(100);
            
            try {
                URL url = new URL("http://www.convcao.com/caoagile/FilesFromAgent/NEPTUS/" + SessionID + "_NewActions.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String jsonClient = in.readLine();
                receive = new Gson().fromJson(jsonClient,TransferData.class);    
                receivedTimestep = receive.timeStep;
             }
             catch(IOException ex) {
                 NeptusLog.pub().error(ex);
             }
        }
        
        showText("Received updated positions from convcao");
        
        timestep++;
        
        for (int AUV = 0; AUV < receive.Location.length; AUV++) {
            String name = nameTable.get(AUV);
            LocationType loc = coords.convert(receive.Location[AUV][0], receive.Location[AUV][1]);
            loc.setDepth(coords.convertNoptilusDepthToWgsDepth(receive.Location[AUV][2]));
            destinations.put(name, loc);
            showText(name+" is being sent to "+receive.Location[AUV][0]+", "+receive.Location[AUV][1]);
        }
        
        myDeleteFile(SessionID + "_Data.txt");
    }
    
    /*
    protected class backgroundWorker extends Thread{
        
        protected backgroundWorker()
        {
            
        }
        
        
        public void run()
        {

            int webClientTimeStep = 0;
            TransferData InD = new TransferData();
            TransferData RecieveData = new TransferData();
            int i = 1;
            while (cancel == false && i<=502)
            {
                

                InD.timeStep = i;
                InD.SessionID = SessionID;
                InD.Bathymeter = new double[AUVS];
                InD.Location = new double[AUVS][3];

                for (int AUV = 0; AUV < AUVS; AUV++)
                {
                    InD.Bathymeter[AUV] = MapAsTable[(int)PosAUVS[AUV][0]][(int)PosAUVS[AUV][1]];
                    InD.Location[AUV][0] = PosAUVS[AUV][0];
                    InD.Location[AUV][1] = PosAUVS[AUV][1];
                    InD.Location[AUV][2] = PosAUVS[AUV][2];
                }

                //display in monitor
                jTextArea1.append(Integer.toString(RecieveData.timeStep) + " || ");
                for (int AUV = 0; AUV < AUVS; AUV++)
                {
                    jTextArea1.append(" [" + Double.toString(InD.Location[AUV][0]) + "],[" + Double.toString(InD.Location[AUV][1]) + "]");
                }
                jTextArea1.append("\n");
                jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
                
                
                Gson gson = new Gson();
                String json = gson.toJson(InD);
                
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(SessionID + "_Data.txt", "UTF-8");
                }
                catch (FileNotFoundException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                writer.write(json);
                writer.close();

                Upload("www.convcao.com","NEPTUS","",jTextField1.getText(),new String(jPasswordField1.getPassword()),SessionID + "_Data.txt");
                
                while (cancel == false &&  i > webClientTimeStep)
                {
                    /*
                    try {
                        Thread.sleep(50);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }* /
                    
                    try {
                        URL url = new URL("http://www.convcao.com/caoagile/FilesFromAgent/NEPTUS/" + SessionID + "_NewActions.txt");
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        String jsonClient = in.readLine();
                        RecieveData = new Gson().fromJson(jsonClient,TransferData.class);    
                        webClientTimeStep = RecieveData.timeStep;
                     }
                     catch(IOException ex) {
                        // there was some connection problem, or the file did not exist on the server,
                        // or your URL was not in the right format.
                        // think about what to do now, and put it here.
                        ex.printStackTrace();
                     }

                    
                }

                PosAUVS = RecieveData.Location;
                i++;
            }

            myDeleteFile(SessionID + "_Data.txt");
            
        }
        
    }
    */
    
    
    /**
     * @param console
     */
    public convcaoNeptusInteraction(ConsoleLayout console) {
        super(console);
        
    }
    
    private double[][] ReadMapfile()
    {
        //TODO but, only for now... 
        
        int DimensionsX, DimensionsY;
        double[][] z = new double[1][1];
        String[] splited;
        
        try{
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstream = new FileInputStream("plugins-dev/caoAgent/convcao/com/caoAgent/mapPorto.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            //Read Header of the map
            strLine = br.readLine();
            splited = strLine.split("\\s+");
            DimensionsX=Integer.parseInt(splited[0]);
            DimensionsY=Integer.parseInt(splited[1]);
            z = new double[DimensionsX+1][DimensionsY+1];
            
            
            //Blank line
            strLine = br.readLine();
            
            //Map parsing 
            for(int i=0;i<DimensionsX;i++)
            {
                strLine = br.readLine();
                splited = strLine.split("\\s+");
                for(int j=0;j<DimensionsY;j++)   
                {
                    z[i][j]=Double.parseDouble(splited[j]);
                }
            }

            //Close the input stream
            in.close();
              }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
            }
        
        return z;
    }
    
    
    
    private String GenerateID()
    {
        Random GetRanomValue = new Random();
        String ID= "Neptus_User_" + Integer.toString(GetRanomValue.nextInt(100000));
        return ID;
    }
    
    
    private void renewButtonActionPerformed(java.awt.event.ActionEvent evt) {
        SessionID = GenerateID();
        
        jTextField1.setEditable(true);
        jPasswordField1.setEditable(true);
       
        
        jLabel1.setVisible(true);
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        
        jTextPane1.setText(SessionID);
        connectButton.setEnabled(true);
        jLabel6.setVisible(true);
    }
    
    
    @SuppressWarnings("deprecation")
    private void StopButtonActionPerformed(java.awt.event.ActionEvent evt) {
        jLabel10.setText("Please Wait...");
        Report = "Canceled";
        
        cancel=true;
        
        
        jLabel9.setVisible(false);
        jButton1.setEnabled(false);
        jButton2.setEnabled(false);
        connectButton.setEnabled(false);
        renewButton.setEnabled(true);
        Report = "";
        jLabel1.setVisible(false);
        jTextArea1.setText("");
        jLabel10.setText("");
        jLabel6.setText("Please Renew your ID to start again");
        
        
    }
    

    private void StartButtonActionPerformed(java.awt.event.ActionEvent evt) {
        cancel = false;
        
        jLabel9.setVisible(true);
        jButton2.setEnabled(true);
        
        jButton1.setEnabled(false);
        renewButton.setEnabled(false);
        connectButton.setEnabled(false);
        timestep = 1;
        controlLoopBackground().start();        
        
        //t1.start();
    }
    
    private void startLocalStructures(String[] vehicles) throws Exception {
        positions.clear();
        destinations.clear();
        bathymetry.clear();
        nameTable.clear();
        arrived.clear();
        
        for (String auvName : vehicles) {            
            EstimatedState state = ImcMsgManager.getManager().getState(auvName).lastEstimatedState();
            if (state == null)
                throw new Exception("Not able to get initial position for vehicle "+auvName);
            LocationType auvPosition = IMCUtils.getLocation(state);
            positions.put(auvName, auvPosition);
            destinations.put(auvName, auvPosition);
            bathymetry.put(auvName, state.getDepth() + state.getAlt()); // FIXME tide offsets
            arrived.put(auvName, true);
            
            
            startControlling(VehiclesHolder.getVehicleById(auvName), ImcMsgManager.getManager().getState(auvName).lastEstimatedState());
        }
        int i = 0;
        for (String v : positions.keySet())
            nameTable.put(i++, v);
    }
    

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) throws SocketException, IOException {
        String[] vehicles = controlledVehicles.split(",");
        
        showText("Starting FollowReference plans on vehicles");
        try {
            ControllerManager manager = new ControllerManager();
            for (String v : vehicles)
                manager.associateControl(this, VehiclesHolder.getVehicleById(v), controlLatencySecs);
        }
        catch (Exception e) {
            GuiUtils.errorMessage(getConsole(), e);
            return;
        }
        
        showText("Initializing Control Structures");
        
        try {
            startLocalStructures(vehicles);
        }
        catch (Exception e) {
            GuiUtils.errorMessage(getConsole(), e);
            return;
        }
        
        
        AUVS = positions.keySet().size();
//        this.PosAUVS = new double[AUVS][3];
//
//        for(int AUV=0;AUV<AUVS;AUV++)
//        {
//            String name = nameTable.get(AUV);
//            LocationType loc = positions.get(name);
//            
//            double[] noptilusCoords = coords.convert(loc);
//            
//            PosAUVS[AUV][0] = noptilusCoords[0];
//            PosAUVS[AUV][1] = noptilusCoords[1];
//            PosAUVS[AUV][2] = coords.convertWgsDepthToNoptilusDepth(positions.get(name).getDepth()); // don't forget this is altitude from deepest point
//        }
//        
//        // FIXME this should work for more than 2 vehicles
//        if (PosAUVS[0][2] == PosAUVS[1][2])
//        {
//            PosAUVS[1][2] += 2;
//        }
        
        // Create path on server
        
        showText("Initializing server connection");
        
        FTPClient client = new FTPClient();

        boolean PathNameCreated=false;
        try {
            client.connect("www.convcao.com",21);
            client.login(jTextField1.getText(), new String(jPasswordField1.getPassword()));
            PathNameCreated = client.makeDirectory("/NEPTUS/" + SessionID);
            client.logout();

        } catch (IOException e) {
            jLabel6.setText("Connection Error");
            throw e;
        }
        
        showText("Sending session data");
        InputData initialState = new InputData();
        initialState.DateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        initialState.SessionID = SessionID;
        initialState.DemoMode = "1";
        initialState.AUVs = ""+positions.keySet().size();
        String fileName = SessionID + ".txt";
        
        Gson gson = new Gson();
        String json = gson.toJson(initialState);
        
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.write(json);
        writer.close();
        
        
           
        if (PathNameCreated)
        {
            jLabel6.setText("Connection Established");
            jLabel1.setVisible(true);
            Upload("www.convcao.com","NEPTUS","",jTextField1.getText(),new String(jPasswordField1.getPassword()),fileName); //send first file
            Upload("www.convcao.com","NEPTUS/" + SessionID,"plugins-dev/caoAgent/convcao/com/caoAgent/",jTextField1.getText(),new String(jPasswordField1.getPassword()),"mapPortoSparse.txt"); //send space map
            jButton1.setEnabled(true);
            jButton2.setEnabled(true);
            jTextPane1.setEditable(false);
            jTextField1.setEditable(false);
            jPasswordField1.setEditable(false);
            connectButton.setEnabled(false);
            renewButton.setEnabled(false);            
        }
        else
        {
            jLabel6.setText(client.getReplyString());
            jLabel1.setVisible(false);
        }
        
        myDeleteFile(fileName);      
        
        showText("ConvCAO control has started");
        
    }
    
    public Thread controlLoopBackground() {
        Thread t = new Thread() {
            public void run() {
                try {
                    controlLoop();
                }
                catch (Exception e) {
                    e.printStackTrace();
                    NeptusLog.pub().error(e);
                }
            };
        };
        return t;
    }
    
    
    private void myDeleteFile(String fileName)
    {
        File f = new File(fileName);
        //delete File with safety
        
        // Make sure the file or directory exists and isn't write protected
        if (!f.exists())
          throw new IllegalArgumentException(
              "Delete: no such file or directory: " + fileName);

        if (!f.canWrite())
          throw new IllegalArgumentException("Delete: write protected: "
              + fileName);

        // If it is a directory, make sure it is empty
        if (f.isDirectory()) {
          String[] files = f.list();
          if (files.length > 0)
            throw new IllegalArgumentException(
                "Delete: directory not empty: " + fileName);
        }

        // Attempt to delete it
        boolean success = f.delete();

        if (!success)
          throw new IllegalArgumentException("Delete: deletion failed");
      
       //end of deleting
    }
    
    
    private void Upload(String ftpServer, String pathDirectory, String SourcePathDirectory, String userName, String password, String filename)
    {

        FTPClient client = new FTPClient();
        FileInputStream fis = null;
        
        try {
            client.connect(ftpServer);
            client.login(userName, password);
            fis = new FileInputStream(SourcePathDirectory+filename);
            //TODO cancel async
            client.changeWorkingDirectory("/"+pathDirectory);
            client.storeFile(filename, fis);
            client.logout();

                
            //Report = "File: " + filename + " Uploaded Successfully ";
        }
        catch (Exception exp)
        {
            Report = "Server Error";
            jLabel6.setText(Report);
        }finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    
    
    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt) throws URISyntaxException, IOException {
        if(java.awt.Desktop.isDesktopSupported() ) {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
     
            if(desktop.isSupported(java.awt.Desktop.Action.BROWSE) ) {
              java.net.URI uri = new java.net.URI("http://www.noptilus-fp7.eu/default.asp?node=page&id=321&lng=2");
                  desktop.browse(uri);
            }
          }
    }
    
    
    /* (non-Javadoc)
     * @see pt.up.fe.dceg.neptus.plugins.SimpleSubPanel#initSubPanel()
     */
    @Override
    public void initSubPanel() {

        jPanelMain = new javax.swing.JPanel();    
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        renewButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        connectButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        jLabel11.setIcon(noptilusLogo);

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel12.setText("<html>www.convcao.com<br>version 0.01</html>");
        jLabel12.setToolTipText("");
        jLabel12.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 19, Short.MAX_VALUE)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setText("Unique ID");

        jTextPane1.setEditable(true);
        jScrollPane1.setViewportView(jTextPane1);
        //jTextPane1.getAccessibleContext().setAccessibleName("");

        renewButton.setText("RENEW");
        renewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renewButtonActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setText("Username");

        jTextField1.setText("FTPUser");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setText("Password");

        jPasswordField1.setText("FTPUser123");

        connectButton.setText("Connect");
        connectButton.setEnabled(false);
        connectButton.setActionCommand("connect");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    connectButtonActionPerformed(evt);
                }
                catch (FileNotFoundException | UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jLabel7.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel7.setText("Command Monitor");

        jButton1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton1.setText("START");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartButtonActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jButton2.setText("STOP");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StopButtonActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 0, 0));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("<html>Click HERE to activate the web service using your ID<br>When the web application is ready, press Start </html>");
        jLabel1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                try {
                    jLabel1MouseClicked(evt);
                }
                catch (URISyntaxException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }   
        });

        //jLabel9.setText("Working...");
        jLabel9.setIcon(runIcon);
        jLabel9.setVisible(false);

        jLabel10.setText("---");

        jLabel6.setForeground(new java.awt.Color(0, 204, 0));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("---");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(126, 126, 126)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(29, 29, 29)
                                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(renewButton))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jPasswordField1)))
                                .addGap(14, 14, 14)
                                .addComponent(connectButton)))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(renewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );

        jLabel1.getAccessibleContext().setAccessibleName("jLabel1");

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 22)); // NOI18N
        jLabel3.setText("Real Time Navigation");

        jLabel8.setIcon(appLogo);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jPanelMain);
        jPanelMain.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 331, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );


        addMenuItem("Tools>Noptilus>Coordinate Settings", ImageUtils.getIcon(PluginUtils.getPluginIcon(getClass())), new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                PluginUtils.editPluginProperties(coords, true);
                coords.saveProps();                
            }
        });
        
        add(jPanelMain);
        
        renewButtonActionPerformed(null);

    }

    /* (non-Javadoc)
     * @see pt.up.fe.dceg.neptus.plugins.SimpleSubPanel#cleanSubPanel()
     */
    @Override
    public void cleanSubPanel() {
        // TODO Auto-generated method stub

    }
    
    // ICONTROLLER METHODS //    
    protected LinkedHashMap<String, LocationType> positions = new LinkedHashMap<>();
    protected LinkedHashMap<String, LocationType> destinations = new LinkedHashMap<>();
    protected LinkedHashMap<String, Double> bathymetry = new LinkedHashMap<>();    
    protected LinkedHashMap<String, Boolean> arrived = new LinkedHashMap<>();
    protected LinkedHashMap<Integer, String> nameTable = new LinkedHashMap<>();
    protected LinkedHashMap<String, Double> depths = new LinkedHashMap<>();
    
    void updateConvCaoService() {
        
    }
    
    @Override
    public String getControllerName() {
        return "convcao";
    }
    
    @Override
    public Reference guide(VehicleType vehicle, EstimatedState estate, FollowRefState frefState) {
        
        if (estate.getAlt() != -1) {
            bathymetry.put(vehicle.getId(), estate.getDepth()+estate.getAlt());            
        }
        positions.put(vehicle.getId(), IMCUtils.parseLocation(estate));
        if (!destinations.containsKey(vehicle.getId())) {
            destinations.put(vehicle.getId(), positions.get(vehicle.getId()));
        }
        
        Reference ref = new Reference();
        ref.setFlags(Reference.FLAG_LOCATION);
        LocationType dest = destinations.get(vehicle.getId());
        dest.convertToAbsoluteLatLonDepth();
        ref.setLat(dest.getLatitudeAsDoubleValueRads());
        ref.setLon(dest.getLongitudeAsDoubleValueRads());
        
        System.out.println("Sending this reference to "+vehicle.getId()+":");
        ref.dump(System.out);
        return ref;
    }
    
    @Override
    public void startControlling(VehicleType vehicle, EstimatedState state) {
        positions.put(vehicle.getId(), IMCUtils.getLocation(state));
        if (state.getAlt() != -1)
            bathymetry.put(vehicle.getId(), state.getDepth()+state.getAlt());
    }
    
    @Override
    public void stopControlling(VehicleType vehicle) {
        this.positions.remove(vehicle.getId());
        this.destinations.remove(vehicle.getId());
        this.bathymetry.remove(vehicle.getId());
    }
    
    @Override
    public boolean supportsVehicle(VehicleType vehicle, EstimatedState state) {
        return vehicle.getType().equalsIgnoreCase("uuv") || vehicle.getType().equalsIgnoreCase("auv"); 
    }
    
    @Override
    public void vehicleTimedOut(VehicleType vehicle) {
        stopControlling(vehicle);
    }
}