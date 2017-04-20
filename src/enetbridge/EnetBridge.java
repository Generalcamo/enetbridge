package enetbridge;

import java.io.File;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(
		modid = "EnetBridge",
		name = "IC2-x Energy Net Bridge",
		version = EnetBridge.VERSION,
		acceptableRemoteVersions = "*",
		dependencies = "required-after:Forge@[10.13.0.1152,);"
				+ "required-after:IC2@[2.2.593,);"
//				+ "after:CoFHAPI|energy;"
//				+ "after:factorization"
		)
public class EnetBridge {
	public static final String VERSION = "2.0";

	public EnetBridge() {
		log = LogManager.getLogger("enetbridge");
	}

	@EventHandler
	public void load(FMLInitializationEvent event) {
		loadConfig();

		if (initRf()) {
			log.info("Loaded RF interface.");
		} else {
			log.info("Didn't load RF interface.");
		}

//		if (initCharge()) {
//			log.info("Loaded Charge interface.");
//		} else {
//			log.info("Didn't load Charge interface.");
//		}

		EventCallback.load();
	}

	public static boolean hasRf() {
		return hasRf;
	}

//	public static boolean hasCharge() {
//		return hasCharge;
//	}

	public static double getRfPerEu() { // eu -> rf
		return rfPerEu;
	}

	public static double getEuPerRf() { // rf -> eu
		return euPerRf;
	}

//	public static boolean getEnableItemAdaption() {
//		return enableItemAdaption;
//	}

	public static boolean getEnableTileEntityAdaption() {
		return enableTileEntityAdaption;
	}
	private File configFolder;
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		configFolder = new File(event.getModConfigurationDirectory(), "enetbridge.ini");
	}

	public static Configuration config;

	private void loadConfig() {

		config = new Configuration(configFolder, "enetconfig.ini");

		try {
			config.load();
		} catch (Exception e) {
			throw new RuntimeException("Error loading base config", e);
		}

		File configFile = new File(new File(configFolder, "config"), "enetbridge.ini");

		try {
			if (configFile.exists()) config.load();
		} catch (Exception e) {
			throw new RuntimeException("Error loading user config", e);
		}

		try {
			if (config.hasChanged()) config.save();
		} catch (Exception e) {
			throw new RuntimeException("Error saving user config", e);
		}

		rfPerEu = config.get("ratios/rfPerEu", "RF per EU ratio", 4).getDouble();
		euPerRf = config.get("ratios/euPerRf", "EU per RF ratio", 0.4).getDouble();
//		chargePerEu = config.get("ratios/chargePerEu").getDouble();
//		euPerCharge = config.get("ratios/euPerCharge").getDouble();
//		targetCharge = config.get("misc/targetCharge").getInt();

//		enableItemAdaption = config.get(ITEM_ADAPTATION,"allow item charging",true).getBoolean();
		enableTileEntityAdaption = config.get(TILE_ADAPTATION, "allow Tile Entity charging", true).getBoolean();
	}

	private boolean initRf() {
		if (rfPerEu <= 0 && euPerRf <= 0) return false;

		try {
			Class.forName("cofh.api.energy.IEnergyConnection");
			Class.forName("cofh.api.energy.IEnergyProvider");
			Class.forName("cofh.api.energy.IEnergyReceiver");
			Class.forName("cofh.api.energy.IEnergyContainerItem");
		} catch (ClassNotFoundException e) {
			log.debug("Can't find class: {}.", e.getMessage());
			return false;
		}

	//	if (getEnableItemAdaption()) {
	//		if (RfItemHandler.load()) {
	//			log.info("Loaded RF item handler.");
	//		} else {
	//			log.info("Can't load RF item handler.");
	//		}
	//	}

		hasRf = true;
		return true;
	}

//	private boolean initCharge() {
//		if (chargePerEu <= 0 && euPerCharge <= 0) return false;
//
//		try {
//			Class.forName("factorization.api.IChargeConductor");
//			Class.forName("factorization.api.Charge");
//		} catch (ClassNotFoundException e) {
//			log.debug("Can't find class: {}.", e.getMessage());
//			return false;
//		}
//
//		hasCharge = true;
//		return true;
//	}


	public static Logger log;

	private static double rfPerEu; // eu -> rf
	private static double euPerRf; // rf -> eu
//	private static final String ITEM_ADAPTATION = "misc/enableItemAdaptation";
	private static final String TILE_ADAPTATION = "misc/enableTileAdaptation";

	private static boolean hasRf = false;
	private static boolean enableItemAdaption;
	private static boolean enableTileEntityAdaption;
}
