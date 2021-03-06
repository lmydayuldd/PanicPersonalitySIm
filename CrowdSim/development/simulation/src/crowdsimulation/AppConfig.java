/*
 * $RCSfile: AppConfig.java,v $ $Date: 2009/07/29 08:50:27 $
 */
package crowdsimulation;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.awt.Color;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;						// Random and HashMap classes
import javax.swing.*;
import javax.xml.parsers.*;
import java.nio.channels.*;

import sim.field.continuous.*;
import math.*;
import crowdsimulation.actioncontroller.*;	// Contains models of behavior for the entities. 
import crowdsimulation.dataanalysis.*;		// Contains techniques for reading in data files.
import crowdsimulation.entities.*;		    // Contains definition of the crowd agents and features.
import crowdsimulation.entities.obstacle.*;	// Contains definition of the crowd agents and features.
import crowdsimulation.logging.*;

import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;
import sim.portrayal.simple.*;
import sim.portrayal.*;

/** 
 * This class is responsible for reading the application configuration from
 * the config.xml file which will be read once on startup.
 *
 * @author $Author: ganil $
 * @version $Revision: 1.23 $
 * $State: Exp $
 * $Date: 2009/07/29 08:50:27 $
 **/
public class AppConfig
{
	
////////////////////////////////////////////////////////////////////////////////
// Attributes
////////////////////////////////////////////////////////////////////////////////

	/** The file name of the Default config file. **/
	//private String fileName = "../configs/config.xml";
        //private String fileName = "../configs/ca_OneExitConfig.xml";
	//private String fileName = "../configs/flockingconfig.xml";
	//private String fileName = "../configs/groupingconfig.xml";
	//private String fileName = "configs/hmfvOneExitConfig.xml";
	//private String fileName = "../configs/Citrus_1.xml";
	//private String fileName = "../configs/Citrus_2.xml";
	//private String fileName = "configs/Citrus_tulane.xml";
	//private String fileName = "../configs/Citrus_tulane_averageAge.xml";
	//private String fileName = "../configs/Citrus_tulane_oldAge.xml";
	//private String fileName = "../configs/Citrus_tulane_youngAge.xml";
	//private String fileName = "../configs/socialPotentialConfig.xml";
	//private String fileName = "../configs/hmfvParamsOneExitConfig.xml";
	//private String fileName = "configs/hmfvMultiExitConfig.xml";
	//private String fileName = "../configs/salamanderConfig.xml";
	  private String fileName = "../configs/salamanderExperimentConfig.xml";
	//private String fileName = "../configs/universalRideConfig.xml";
	//private String fileName = "../configs/lkfOneExitConfig.xml";
	//private String fileName = "../configs/lkfOneExitOriginalRoomSizeConfig.xml";
	//private String fileName = "../configs/lkfWithPersonalityVectorOneExitConfig.xml";
	//private String fileName = "../configs/lkfOneExitConfig.xml";
	//private String fileName = "../configs/rayTrace_hmfvOneExitConfig.xml";
        //private String fileName = "configs/Church_1.xml";
        //private String fileName = "../configs/Church_Hisp.xml";
	//private String fileName = "configs/SimLibCAModelConfig.xml";
	
	private String logFolder = "../logs";

	/** Local version of the regions to be populated and set in CrowdSimulation. **/
	private Continuous2D terrainCopy;

	/** The action controller for the obstacles. **/
	private ActionController obstacleModel;	

	/** A collection of all the paths. **/
	private HashMap paths;
	/** A collection of all random generators. **/
	private HashMap randomGenerators;
	
	/** The instance of CrowdSimulation, which controls the entire simulation. **/
	private CrowdSimulation crowdSim;
	
////////////////////////////////////////////////////////////////////////////////
// Methods
////////////////////////////////////////////////////////////////////////////////
    public static void copyFile(File in, File out) 
        throws IOException 
    {
        FileChannel inChannel = new
            FileInputStream(in).getChannel();
        FileChannel outChannel = new
            FileOutputStream(out).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(),
				 outChannel);
        } 
        catch (IOException e) {
            throw e;
        }
        finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
        }
    }

	/** 
	 * Configures the simulation based on a config filename.
	 * The crowdsim object will be initialized with all the information
	 * from the configuration file.
	 *
	 * @param file The name of the file which contains the configuration data.
	 * @param crowdSim The CrowdSimulation object which is to be configured.
	 */
	public AppConfig( String file, CrowdSimulation crowdSim )
	{
		this.crowdSim = crowdSim;
		
		if( file != null )
		{
			fileName = file;
		}

		this.obstacleModel = new ObstacleModel();
		this.paths = new HashMap();
		this.randomGenerators = new HashMap();
	}
	
	/**
	 * This is the method which reads the XML config file and constructs the 
	 * world, terrain, obstacles, and individuals.
	 **/
	public void initializeCrowdSimulation()
	{
		System.out.println("Appconfig initialization");
		try 
		{
		    // Parse the XML as a W3C document.
		    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document document = builder.parse( new File(fileName) );
			Node rootNode = document.getDocumentElement();    
		    traverseDocument(rootNode);
		    try{
			File targetFile = new File(logFolder + System.getProperty("file.separator") + "config.xml");
			targetFile.getParentFile().mkdirs();
			copyFile(new File(fileName),targetFile);
		    } catch(java.io.IOException e) {
			Log.log( 1, "Unable to copy configuration file" );
			Log.log( 1, e );
		    }
		} 
		catch (ParserConfigurationException e) 
		{
			System.err.println("ParserConfigurationException caught...");
		    e.printStackTrace();
		    System.exit( -1 );
		} 
		catch (SAXException e) 
		{
		    System.err.println("SAXException caught...");
		    e.printStackTrace();
		    System.exit( -1 );
		} 
		catch (IOException e) 
		{
		    System.err.println("IOException caught...");
		    e.printStackTrace();
		    System.exit( -1 );
		}
	}
	
	/**
	 * This function is called recursively to traverse nodes of the XML document.
	 *
	 * @param n The current node that is being processed from the config settings.
	 */
	private void traverseDocument(Node n)
	{
		if (n.getNodeType()==Node.ELEMENT_NODE)
		{
			String nodeName = n.getNodeName();
			Element currentElement = (Element)n;
			
			if( nodeName.equalsIgnoreCase( "log" ) )
			{
				createLog( n );
			}
			if( nodeName.equalsIgnoreCase( "logFolder" ) )
			{
				String s = n.getFirstChild().getNodeValue();
				if(s != null) {
				    logFolder = s;
				}
			}
			else if( nodeName.equalsIgnoreCase( "world" ) )
			{
                            System.out.println( "AppConf-ln203: Entering node = world " );                            
				try
				{
					int width = Integer.parseInt( currentElement.getAttribute( "width" ) );
					int height = Integer.parseInt( currentElement.getAttribute( "height" ) );
					int discretization = Integer.parseInt( currentElement.getAttribute( "discretization" ) );
					
					double scale = 1;
					if( currentElement.hasAttribute( "scale" ) )
					{
						scale = Double.parseDouble( currentElement.getAttribute( "scale" ) );
					}
					
					crowdSim.worldWidth = width;
					crowdSim.worldHeight = height;
					crowdSim.setWorld( new Continuous2D( discretization, width, height ) );
					crowdSim.setTerrain( new Continuous2D( discretization, width, height ) );
					crowdSim.setWorldInfo( new Continuous2D( discretization, width, height ) );
					crowdSim.setVisualScale( scale );
			                // terrainCopy is only created once. 
					this.terrainCopy = new Continuous2D( discretization, width, height );
                            System.out.println( "AppConf-ln224: Just created new terrainCopy " );                            
				}
				catch(NumberFormatException e)
				{
					Log.log( 1, "Unable to initialize world settings properly..." );
					Log.log( 1, "   width, height and discretization must all be integer values." );
					Log.log( 1, e );
					System.exit( -1 );
				}	
			}
			else if( nodeName.equalsIgnoreCase( "obstacles" ) )
			{
                                System.out.println("AppConf-ln235: Entering node = Obstacles ");                            
 				this.createObstacles( n, this.crowdSim.getTerrain() );
			}
			else if( 
					nodeName.equalsIgnoreCase( "paths" )&&
					!n.getParentNode().getNodeName().equalsIgnoreCase( "individuals" ) )
			{
				this.createPaths( n );
			}
			else if( nodeName.equalsIgnoreCase( "randomGenerators" ) )
			{
				Node[] generators = this.getChildNodes( n, "generator" );
				for( int i = 0; i < generators.length; i++ )
				{
					this.createRandomGenerator( generators[i] );
				}
			}
			else if( nodeName.equalsIgnoreCase( "model" ) )
			{
//                  System.out.println( "AppConf-ln253: Entering node = model " );                            
				this.createPrimaryActionController( n );
			}
			else if( nodeName.equalsIgnoreCase( "movie" ) && crowdSim.getUI() != null )
			{
				//System.out.println("Got movie tag, ui = "+crowdSim.getUI());
				crowdSim.setCaptureMovie( true );
				crowdSim.otherframe = new JFrame( );
        		crowdSim.panel = new JPanel();
        		crowdSim.otherframe.getContentPane().add( crowdSim.panel );
        
    			crowdSim.otherframe.show();    
    			crowdSim.otherframe.resize( 640, 480 );
    			
				if( currentElement.hasAttribute( "fileName" ) )
				{
					crowdSim.setMovieName( currentElement.getAttribute( "fileName" ) );
				}
				if( currentElement.hasAttribute( "type" ) )
				{
					crowdSim.setMovieType( currentElement.getAttribute( "type" ) );
				}
				if( currentElement.hasAttribute( "frameRate" ) )
				{
					try
					{
						crowdSim.setFrameRate( Integer.parseInt( currentElement.getAttribute( "frameRate" ) ) );
					}
					catch( NumberFormatException nfe )
					{
						Log.log( 0, "The framerate for recording a video must be represented as a number!" );
						System.exit( -1 );
					}
				}
			}
			else if( nodeName.equalsIgnoreCase( "terminate" ) )
			{
				if( currentElement.hasAttribute( "time" ) )
				{
					crowdSim.setDuration( Double.parseDouble( currentElement.getAttribute( "time" ) ) );
					crowdSim.setTerminate(false);
				}
				else 
					crowdSim.setTerminate(true);
			}		    
		    // For loop used to traverse the XML document.
			for( Node c=n.getFirstChild(); c!=null; c=c.getNextSibling() )
			{
				traverseDocument( c );
			}
		}
	}
	
	/**
	 * Create a log for the simulation.
	 * The Log node is passed in and we process it.
	 *
	 * @param logNode The node representing Log from the file.
	 **/
	private void createLog( Node logNode )
	{
		Class[] paramList = {(new String()).getClass()};
		Element currentElement = (Element)logNode;
		String fileName = logFolder + System.getProperty("file.separator") + currentElement.getAttribute( "fileName" );
		Log.setConsoleLogFileName(logFolder + System.getProperty("file.separator") + "system.log");
		String logType = currentElement.getAttribute( "type" );
		System.out.println("Log: " + fileName);		
		Object[] params = {(Object)fileName};
		
		if( logType.equals( "SystemLog" ) )
		{
			Log log = new Log( fileName );
			log.setLogLevel( Integer.parseInt( currentElement.getAttribute( "logLevel") ) );
			log.setConsoleOutput( Boolean.parseBoolean( currentElement.getAttribute( "consoleOutput" ) ) );
			log.setFileOutput( Boolean.parseBoolean( currentElement.getAttribute( "fileOutput" ) ) );
			return;
		}
		
		Logger instance = null;
		try
		{
			Class logClass = Class.forName( "crowdsimulation.logging." + logType );
			Constructor logCreator = logClass.getConstructor( paramList );
			instance = (Logger)logCreator.newInstance( params );
		}
		catch( Exception e )
		{
			System.out.println( "Class Not Found : crowdsimulation.logging."+logType   );
			e.printStackTrace();
			System.exit( -1 );
		}
		
		HashMap logParams = new HashMap();
		this.setParameters( logNode, logParams );
		
		instance.setParameters( logParams );
		crowdSim.addLogger( instance );
	}
	
	/**
	 * Create all the "interacting" obstacles in the simulation.
	 * The Obstacles node is passed in and we get an 
	 * array of Obstacle child nodes and process them.
	 *
	 * @param obstaclesNode The node representing Obstacles from the file.
	 * @param terrain The terrain to which obstacles are added.
	 **/
	private void createObstacles( Node obstaclesNode, Continuous2D terrain )
	{
		Node[] obstacleNodes = this.getChildNodes( obstaclesNode, "obstacle" );
		
		for( int i = 0; i < obstacleNodes.length; i++ )
		{
			this.createObstacle( obstacleNodes[i], terrain, this.obstacleModel );
    		}	
	}
		
	/**
	 * Create the obstacle by calling the entity factory createObstacle() method.
	 *
	 * @param obstacleNode The xml node which contains the data for the obstacle.
	 * @param terrain The terrain on which the obstacle should be placed.
	 * @param control The Action controller which should control the obstacle.
	 * @return The newly created obstacle.
	 **/
	private Obstacle createObstacle( Node obstacleNode, Continuous2D terrain, ActionController control )
	{
		HashMap parameters = new HashMap();
		
		// Set the terrain object.
		parameters.put( "terrain", terrain );    
		
		// Set the obstacles' properties in a HashMap, such as radius, width, location, color or center. 
		this.setParameters( obstacleNode, parameters );
                System.out.println( "AppConf-ln390: Entering EntFac for obstacle" ); 		
		return EntityFactory.getInstance().createObstacle( control, parameters );	
	}
	
        /**
	 * Creates the region by calling the entity factory createObstacle() method.
	 *
	 * @param obstacleNode The xml node which contains the data for the obstacle.
	 * @param terrain The (Continuous2D)region on which the region should be placed.
	 * @param control The Action controller which should control the obstacle.
	 * @return The newly created obstacle.
	 **/
	private Obstacle createRegion( Node obstacleNode, Continuous2D terrain, ActionController control )
	{
		HashMap parameters = new HashMap();
		
		// Set the terrain object.
		parameters.put( "region", terrain );    
		
		// Set the obstacles' properties in a HashMap, such as radius, width, location, color or center. 
		this.setParameters( obstacleNode, parameters );
		
		return EntityFactory.getInstance().createObstacle( control, parameters );	
	}

	/**
	 * Create paths from the data in the config file and add it to the worldInfo object.
	 * 
	 * @param pathNode The xml node which contains the data for the paths.
	 **/
	private void createPaths( Node pathNode )
	{
		Node[] pathNodes = this.getChildNodes( pathNode, "path" );
		
		for( int i = 0; i < pathNodes.length; i++ )
		{
			Path path = this.createPath( pathNodes[i] );
			if( path != null )
			{
				// Add path to the collection of paths
				paths.put(new Integer( path.getID() ), path);
				crowdSim.getPaths().put(new Integer( path.getID() ), path);				
			}
		}
	}

	/**
	 * Create a path from the data in the config file and
	 * add it to the worldInfo object.
	 *
	 * @param pathNode The xml node which contains the data for the path.
	 * @return The newly created path.
	 **/
	private Path createPath( Node pathNode )
	{
		Path path = null;
				
		try
		{
			// Get the path properties from the attributes in the XML file.
			int pathID = Integer.parseInt( ((Element)pathNode).getAttribute( "id" ) );
			String pathName = ((Element)pathNode).getAttribute( "name" );
			String adaptive = ((Element)pathNode).getAttribute( "adaptive" );

			if(adaptive.equalsIgnoreCase("true"))
                path = new AdaptivePath();
            else path = new Path();
			// Set the path properties from values obtained in the XML file.
			path.setID( pathID );
			
					
			// Loop through the inner <waypoint> elements and add them to the path.
			Node[] waypoints = getChildNodes( pathNode, "waypoint" );

			if( waypoints != null )
			{
				for( int i = 0; i < waypoints.length; i++ )
				{
					path.addWaypoint( this.createWayPoint( waypoints[i] ) );		
				}
			}

		}
		catch( NumberFormatException e )
		{
			Log.log( 1, "Unable to initialize path properly..." );
			Log.log( 1, "   PathID must be an integer value" );
			Log.log( 1, e );
			System.exit( -1 );
		}
		
		// Adding all paths to worldInfo object.
 		// You can toggle the view of the paths in Mason's GUI.
 		this.crowdSim.getWorldInfo().setObjectLocation( (Object)path, new Double2D( 0,0 ) );
 		return path;
	}
	
	/**
	 * Creates a waypoint from the Waypoint element.
	 *
	 * @param waypointNode The xml node which contains the data for the waypoint.
	 * @return A waypoint initialized from the Waypoint element's attributes.
	 **/ 
	private Waypoint createWayPoint( Node waypointNode )
	{
		Waypoint waypoint = null;
		
		try
		{
            String type = ((Element)waypointNode).getAttribute( "type" );
            //System.out.println(((Element)waypointNode).getAttribute( "type" ));
            if( type == null || type.equalsIgnoreCase("circle") || type.equalsIgnoreCase("")) {
                double x = Double.parseDouble( ((Element)waypointNode).getAttribute( "x" ) );
                double y = Double.parseDouble( ((Element)waypointNode).getAttribute( "y" ) );
                double radius = Double.parseDouble( ((Element)waypointNode).getAttribute( "radius" ) );
                waypoint = new CircularWaypoint( this.crowdSim.getWorldInfo(), x, y, radius );
                //System.out.println("Made CWP");
            } else if (type.equalsIgnoreCase("line") || type.equalsIgnoreCase("kaup_line")) {
                double x = Double.parseDouble( ((Element)waypointNode).getAttribute( "x" ) );
                double y = Double.parseDouble( ((Element)waypointNode).getAttribute( "y" ) );
                double length = Double.parseDouble( ((Element)waypointNode).getAttribute( "length" ) );
                double angle = Double.parseDouble( ((Element)waypointNode).getAttribute( "angle" ) );
                double left = Double.MIN_VALUE;
                double right = Double.MIN_VALUE;
                if((!((Element)waypointNode).getAttribute( "left" ).equalsIgnoreCase("")) && (!((Element)waypointNode).getAttribute( "right" ).equalsIgnoreCase("")) ) {
                    left = Double.parseDouble( ((Element)waypointNode).getAttribute( "left" ) );
                    right = Double.parseDouble( ((Element)waypointNode).getAttribute( "right" ) );
                    if(type.equalsIgnoreCase("kaup_line"))
                        waypoint = new KaupLineWaypoint(this.crowdSim.getWorldInfo(), x, y, length, angle,left,right);
                    else
                        waypoint = new LineWaypoint(this.crowdSim.getWorldInfo(), x, y, length, angle,left,right);
                } else
                    if(type.equalsIgnoreCase("kaup_line"))
                        waypoint = new KaupLineWaypoint(this.crowdSim.getWorldInfo(), x, y, length, angle);
                    else
                        waypoint = new LineWaypoint(this.crowdSim.getWorldInfo(), x, y, length, angle);
                //System.out.println("Made LWP");
            }
		}
		catch( NumberFormatException e )
		{
			Log.log( 1, "Unable to initialize waypoint properly..." );
			Log.log( 1, "   a, and y must all be integer values." );
			Log.log( 1, e );
			System.exit( -1 );
		}
		
		return waypoint;
	}
	
	/**
	 * Create the primary action model based on the string passed in.
	 *
	 * @param modelNode The xml node which contains the data for the ActionController.
	 **/
	private void createPrimaryActionController( Node modelNode )
	{
		Element currentElement = (Element)modelNode;
		String modelType = currentElement.getAttribute( "type" );
		
		ActionController newModel = null;
		
		Class[] paramList = {(new String()).getClass()};
		String fileName = currentElement.getAttribute( "fileName" );
		String logType = currentElement.getAttribute( "type" );
//TODO: Remove the next three lines when done with debug.                 
//                System.out.println( "AppConf-ln548: The modelType is = " + modelType );                            
//                System.out.println( "AppConf-ln553: The fileName is = " + fileName );                            
//                System.out.println( "AppConf-ln554: The logType is = " + logType );                            
		
		Object[] params = {(Object)fileName};
		Class[] classEmptySet = {};
		Object[] objectEmptySet = {};
		try
		{
			Class modelClass = Class.forName( "crowdsimulation.actioncontroller." + modelType );
			Constructor modelCreator = modelClass.getConstructor( classEmptySet );
			newModel = (ActionController)modelCreator.newInstance( objectEmptySet );
		}
		catch( Exception e )
		{
			System.out.println( "Class Not Found : crowdsimulation.actioncontroller." + modelType   );
			e.printStackTrace();
			System.exit( -1 );
		}
		
		HashMap modelParams = new HashMap();
		this.setParameters( modelNode, modelParams ); //This sets the <individuals node into modelParams.
		newModel.setParameters( modelParams );

		this.setPrimaryActionController( newModel );  // Here is where the ActionController is set, which appears to be empty. 
		this.crowdSim.schedule(newModel);
		
		Bag definedIndividuals = new Bag();

		if( modelParams.containsKey( "dataFile" ) )
		{
			double startTime = 0;
			if( modelParams.containsKey( "startTime" ) )
			{
				startTime = Double.parseDouble( (String)modelParams.get( "startTime" ) );
			}
			definedIndividuals = readDataFile
				( 
					(String)modelParams.get( "dataFile" ), 
					startTime, 
					newModel.getClass().getSimpleName() 
				);
		}
		
		Node[] individuals = this.getChildNodes( modelNode, "individuals" );
		
		if( definedIndividuals.size() > 0 )          
		{            // This option is only used if definedIndividuals is nonempty. 
			this.createIndividuals( individuals[0], definedIndividuals );
		}
		else
		{
			for( int i = 0; i < individuals.length; i++ ) 
			{              // This option is when no individuals were read in from data file.
				this.createIndividuals( individuals[i] );
			}
		}
	}
	
	/**
	 * Create a random generator with properties read from the config file.
	 *
	 * @param generatorNode The xml node which contains the data for the randomGenerator.
	 **/
	private void createRandomGenerator( Node generatorNode )
	{
		// Grab the attributes of the <generator att1="" att2=""/> node.
		HashMap parameters = new HashMap();
		this.setParameters( generatorNode, parameters );
		
		RandomGenerators newGenerator = null;
		
		String type = (String)parameters.get( "type" );
		int id = Integer.parseInt( (String)parameters.get( "id" ) );
		if( type.compareToIgnoreCase( "binomial" ) == 0 )
		{
			double probSuccess = Double.parseDouble( (String)parameters.get( "probSuccess" ) );
			int numTrials = Integer.parseInt( (String)parameters.get( "probSuccess" ) );
			
			newGenerator = new BinomialDistributedGenerator( probSuccess, numTrials );	
		}
		else if( type.compareToIgnoreCase( "exponential" ) == 0 )
		{
			newGenerator = new ExponentialDistributedGenerator();	
		}
		else if( type.compareToIgnoreCase( "exponentialWithMean" ) == 0 )
		{
			double mean = Double.parseDouble( (String)parameters.get( "mean" ) );
			newGenerator = new ExponentialWithMeanDistributedGenerator(mean);	
		}
		
		else if( type.compareToIgnoreCase( "gamma" ) == 0 )
		{
			int ithEvent = Integer.parseInt( (String)parameters.get( "event" ) );
			newGenerator = new GammaDistributedGenerator( ithEvent );
		}
		else if( type.compareToIgnoreCase( "normal" ) == 0 )
		{
			double mean = Double.parseDouble( (String)parameters.get( "mean" ) );
			double stdDev = Double.parseDouble( (String)parameters.get( "stdDev" ) );
			newGenerator = new NormalDistributedGenerator( mean, stdDev );
		}
		else if( type.compareToIgnoreCase( "poisson" ) == 0 )
		{
			double mean = Double.parseDouble( (String)parameters.get( "mean" ) );
			newGenerator = new PoissonDistributedGenerator( mean );
		}
		else if( type.compareToIgnoreCase( "uniform" ) == 0 )
		{
			double min = Double.parseDouble( (String)parameters.get( "min" ) );
			double max = Double.parseDouble( (String)parameters.get( "max" ) );
			newGenerator = new UniformDistributedGenerator( min, max );
		}
		//??? Alan Jolly m-Erlang Distribution
		else if( type.compareToIgnoreCase( "erlang" ) == 0 )
		{
			int m = Integer.parseInt((String)parameters.get( "m" ) );
			float mean = Float.parseFloat((String)parameters.get( "mean" ) );
			newGenerator = new ErlangDistributedGenerator( m, mean );
		}
		else
		{
			Log.log( 1, "A Generator of type:" + type +" does not exist!"  );
			Log.log( 1, "   Please set to a valid random number generator"  );
			System.exit( 1 );
		}
		
		// Add the new random generator to the list of generators.
		if( newGenerator != null )
		{
			this.randomGenerators.put( new Integer( id ), newGenerator );	
		}
	}
	
	/**
	 * This method creates a set of individuals from an individual's node.
	 * This cycles through the individuals node and creates all individuals
	 * in the defined area, along with setting any paths or other attributes 
	 * which are associated with this node.
	 * 
	 * @param individualNode The node containing the information for the individuals.
	 * @param individualsData The data for the individuals to be created.
	 **/
	private void createIndividuals( Node individualNode, Bag individualsData )
	{
		try
		{
			// Determine the number of individuals to create.
			int numberOfIndividuals = Integer.parseInt( ((Element)individualNode).getAttribute( "number" ) );
			
			// Determine the starting region for the individuals. 
			Obstacle startingRegion = null;
			Node[] regions = this.getChildNodes( individualNode, "region" );
			if( regions.length > 0 )
			{
				Node[] obstacles = this.getChildNodes( regions[0], "obstacle" );
//TODO: Check with Rex on this change from createObstacle to creatRegion, if anything goes wrong.                                 
				startingRegion = this.createRegion( obstacles[0], this.terrainCopy, null );
			}
			
			// Set the individual's ActionController properties in a HashMap.
			HashMap parameters = new HashMap();
			this.setParameters( individualNode, parameters );

			// Add a reference to the individual's paths to the parameters map.
			Bag indPaths = this.createPathsForIndividuals( individualNode );
			parameters.put( "paths", indPaths );
			
			// Add a reference to the portrayal object which determines what an individual looks like.
			OrientedPortrayal2D indPortrayal = this.createPortrayalForIndividuals( parameters );
			parameters.put( "portrayal", indPortrayal );

			// Create the individuals.
			for( int i = 0; i < individualsData.size(); i++ )
			{
				HashMap params = (HashMap)parameters.clone();
				HashMap indData = (HashMap)individualsData.get( i );
				
				params.put( "diameter", indData.get( "diameter" ) );
				params.put( "location", new Vector2D( Double.parseDouble( (String)indData.get( "X" ) ), Double.parseDouble( (String)indData.get( "Y" ) ) ) );
				params.put( "velocity", new Vector2D( Double.parseDouble( (String)indData.get( "velocityX" ) ), Double.parseDouble( (String)indData.get( "velocityY" ) ) ) );
				params.put( "orientation", indData.get( "orientation" ) );
				params.put( "paths", indData.get( "paths" ) );
				params.put( "pathID", indData.get( "pathID" ) );
				params.put( "wayPointLocation", new Vector2D( Double.parseDouble( (String)indData.get( "wayPointX" ) ), Double.parseDouble( (String)indData.get( "wayPointY" ) ) ) );
				params.put( "groupID", indData.get( "groupID" ) );
				params.put( "color", indData.get( "color" ) );
				params.put( "initialVel", indData.get( "initialVel" ) );

				EntityFactory.getInstance().createIndividuals
					(
						1, 
						startingRegion,
						this.getPrimaryActionController(),
						params 
					);
			}
		}
		catch( NumberFormatException e )
		{
			Log.log( 1, "Unable to initialize individuals properly..." );
			Log.log( 1, "   numberOfIndividuals must be an integer value." );
			Log.log( 1, "   time, if used, must be a double value." );
			Log.log( 1, e );
			System.exit( -1 );
		}
	}
	
	/**
	 * Create a certain number of individuals with properties read from the config file.
	 *
	 * @param individualNode The xml node which contains the data for the individuals to be created.
	 */
	private void createIndividuals( Node individualNode )
	{	
		try
		{
			// Determine the number of individuals to create.
			int numberOfIndividuals = Integer.parseInt( ((Element)individualNode).getAttribute( "number" ) );
			
			// Determine the starting region for the individuals. 
			Obstacle startingRegion = null;
			Node[] regions = this.getChildNodes( individualNode, "region" );
			if( regions.length > 0 )
			{
				Node[] obstacles = this.getChildNodes( regions[0],"obstacle" );
				startingRegion = this.createRegion( obstacles[0], this.terrainCopy, null );
			}
			
			// Set the individual's ActionController properties in a HashMap.
			HashMap parameters = new HashMap();
			this.setParameters( individualNode, parameters );

			// Add a reference to the individual's paths to the parameters map.
			Bag indPaths = this.createPathsForIndividuals( individualNode );
			parameters.put( "paths", indPaths );
			
			// Add a reference to the portrayal object which determines what an individual looks like.
			OrientedPortrayal2D indPortrayal = this.createPortrayalForIndividuals( parameters );
			parameters.put( "portrayal", indPortrayal );

			// Create the individuals.
			if( parameters.containsKey( "time" ) )
			{
				Object value = parameters.get( "time" );
				if(value instanceof RandomGenerators)
				{
					EntityFactory.getInstance().createIndividuals
						( 
						    ((RandomGenerators)value),
							numberOfIndividuals, 
							startingRegion,
							this.getPrimaryActionController(),
							parameters
						);
				}
				else
				{
					EntityFactory.getInstance().createIndividuals
						( 
							Double.parseDouble( (String)parameters.get( "time" ) ),
							numberOfIndividuals, 
							startingRegion,
							this.getPrimaryActionController(),
							parameters
						);
				}
			}
			else
			{
				EntityFactory.getInstance().createIndividuals
					(
						numberOfIndividuals, 
						startingRegion,
						this.getPrimaryActionController(),
						parameters
					);
			}
		}
		catch( NumberFormatException e )
		{
			Log.log( 1, "Unable to initialize individuals properly..." );
			Log.log( 1, "   numberOfIndividuals must be an integer value." );
			Log.log( 1, "   time, if used, must be a double value." );
			Log.log( 1, e );
			System.exit( -1 );
		}
	}
	
	/**
	 * Create the portrayal object for the set of individuals and set the color
	 * to the color object in the parameters list.
	 *
	 * @param parameters The individual's parameters containing the color object.
	 * @return OrientedPortrayal2D with the specified color is returned.
	 **/
	private OrientedPortrayal2D createPortrayalForIndividuals( HashMap parameters )
	{
		Color indColor = Color.pink;
		if( parameters.containsKey( "color" ) )
		{
			indColor = 	(Color)parameters.get( "color" );
		}
		
		OrientedPortrayal2D portrayal = new OrientedPortrayal2D
			(
				new SimplePortrayal2D(),
				0,
				4.0,
			    indColor,
			    OrientedPortrayal2D.SHAPE_COMPASS
			);
		
		return portrayal;
	}
	
	/**
	 * Creates a collection of paths to be associated to an individuals.
	 *
	 * @param individualNode The xml node which contains the data for the individuals.
	 * @return A Bag of the paths created for the indiviudals.
	 **/
	private Bag createPathsForIndividuals( Node individualNode )
	{
		Bag individualsPaths = new Bag();
		
		// Add the individual's paths to the individual
		Node[] pathsNode = this.getChildNodes( individualNode, "paths" );
		if( pathsNode.length > 0)
		{
			Node[] individualPaths = this.getChildNodes( pathsNode[0], "path" );
			
		    for( int j = 0; j < individualPaths.length; j++ )
		    {
		    		int pathID = Integer.parseInt( ((Element)individualPaths[j]).getAttribute( "id" ) );
					double pathWeight = Double.parseDouble( ((Element)individualPaths[j]).getAttribute( "weight" ) );
					
					Path newPath = (Path)((Path)paths.get( new Integer( pathID ) )).clone();
					newPath.setWeight( pathWeight );
                    System.out.println("Path Weight = "+newPath.getWeight());
					
					individualsPaths.add( newPath );
		    }
		} 
		
		return individualsPaths;
	}
	
	/**
	 * Pass in a node and a parameters hash map. All the node's
	 * attributes will be stored in the parameters object as key,value
	 * pairs.  Certain values will be objects other than strings based
	 * on the key name.
	 *
	 * @param node The node whose attributes name and values we want to store.
	 * @param parameters The hash map to place the node's attributes names and values.
	 **/
	private void setParameters( Node node, HashMap parameters )
	{
		Element element = (Element)node;
		// Get all the attributes of an element in a map.
		NamedNodeMap attributes = element.getAttributes();
		
		// Get number of attributes in the element.
		int numAttributes = attributes.getLength();
		
		// Process each attribute.
		for( int i = 0; i < numAttributes; i++ )
		{
			Attr  attr = (Attr)(attributes.item( i ));
			
			// Get attribute name and value.
			String key = attr.getNodeName();
			String value = attr.getNodeValue();
			
			// Check if value starts with 'genId='
			if( value.startsWith( "genID=" ) )
			{
				// Add the appropriate random generator to the properties collection.
				int id = Integer.parseInt( value.substring( 6 ) );
				
				parameters.put( key, randomGenerators.get( new Integer( id ) ) );
			}
			else
			{
				// Add the attribute to the hash map.
				if( key.equalsIgnoreCase( "color" ) )
				{
					Color color;
					String[] rgb = value.split( " " );
					int red = Integer.parseInt( rgb[0] );
					int green = Integer.parseInt( rgb[1] );
					int blue = Integer.parseInt( rgb[2] );
					color = new Color( red, green, blue );
					
					parameters.put( key, color );
				}
				else if
					( 
						key.equalsIgnoreCase( "location" ) ||
						key.equalsIgnoreCase( "center" ) 
					)
				{
					String[] coords = value.split( " " );
					double x = Double.parseDouble( coords[0] );
					double y = Double.parseDouble( coords[1] );
					
					parameters.put( key, new Vector2D( x,y ) );
				}
				else
				{
					parameters.put( key, value );	
				}
			}
	    }		
	}
	
	/**
	 * Helper function to get an array of child nodes by a string name.
	 *
	 * @param currentNode The xml node of the node to find the child node on.
	 * @param childNodeName The name of the xml child node.
	 * @return The child nodes with the name childeNodeName are retruned.
	 *	Null is returned if a matching child node isn't found. 
	 **/
	private Node[] getChildNodes( Node currentNode, String childNodeName )
	{
		ArrayList children = new ArrayList();
		
		Node child = currentNode.getFirstChild();
		while( child != null )
		{
				String childName = child.getNodeName();	
				if( childName.equalsIgnoreCase( childNodeName ) )
				{
						children.add( child );
				}	
				child = child.getNextSibling();
		}
		
		Node[] childArray = new Node[children.size()]; 
		children.toArray( childArray );
		
		return childArray;
	}
	
	/**
	 * Reads in a comma separated datafile to allow for restarting a simulation from a previous data set.
	 * Can be used to create specific configurations of individuals and run the simulation from that 
	 * starting configuration.
	 *
	 * @param dataFile The name of the file containing the data to load in.
	 * @param startTime The time to use when seeing what data to load in.
	 *   All data for this time will be loaded in and the individuals which exist at this time 
	 *   will be created and given all the information which is in the datafile.
	 * @param modelName The name of the model which should be controlling these individuals.
	 * @return A Bag containing all of the data to recreate the individuals from the datafile.
	 **/
	public Bag readDataFile( String dataFile, double startTime, String modelName )
	{
		Bag data = new Bag();
		
		double time = 0;
		double recordTime = 0;
		CSVAnalysis dataReader = new CSVAnalysis( dataFile );
		String[] keys = (String[])dataReader.nextLine();
		String[] strData = new String[0];
		
		HashMap indData = new HashMap();
		
		while( time <= startTime && strData != null )
		{
			strData = (String[])dataReader.nextLine();
			if( strData != null )
			{
				time = Double.parseDouble( (String)strData[1] );
			}
		}

		recordTime = time;
		
		while( time == recordTime )
		{
			if( strData != null )
			{
				for( int i =0; i < keys.length; i++ )
				{
					indData.put( keys[i], strData[i] );
				}

				if( time == recordTime && indData.get( "model" ).equals( modelName ) )
				{
					data.add( indData.clone() );
				}

			}
			strData = (String[])dataReader.nextLine();
			time = Double.parseDouble( (String)strData[1] );
		}

		return data;
	}
	 
////////////////////////////////////////////////////////////////////////////////
// Accessors and Mutators
////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the primary action model for the individuals.
	 *
	 * @return The primary action model.
	 **/
	public ActionController getPrimaryActionController()
	{
		return this.crowdSim.getPrimaryActionController();
	}
	
	/**
	 * Sets the primary action model in the crowd simulation.
	 *
	 * @param val The primary action model to be used in the crowd simulation.
	 **/
	public void setPrimaryActionController( ActionController val )
	{
		this.crowdSim.setPrimaryActionController( val );
	}
}
