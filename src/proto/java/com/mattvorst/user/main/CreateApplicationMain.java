package com.mattvorst.user.main;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.mattvorst.shared.constant.CountryCode;
import com.mattvorst.shared.constant.EnvironmentConstants;
import com.mattvorst.shared.constant.Status;
import com.mattvorst.shared.dao.SecurityDao;
import com.mattvorst.shared.dao.model.security.SourceUser;
import com.mattvorst.shared.dao.model.security.User;
import com.mattvorst.shared.dao.model.security.UserPassword;
import com.mattvorst.shared.security.constant.Source;
import com.mattvorst.shared.util.Environment;
import com.mattvorst.shared.util.Utils;
import com.thebridgetoai.website.dao.DataStoreDao;
import com.thebridgetoai.website.model.DataStore;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CreateApplicationMain {

	public static void main(String[] args) {

		Environment.instance(EnvironmentConstants.ENV_VORST);

		DataStoreDao dataStoreDao = new DataStoreDao("thebridgetoai");

		DataStore dataStore = new DataStore();
		dataStore.setApplicationUuid(UUID.fromString("a6292bac-855b-4036-8d66-8a85416b85d4"));
		dataStore.setNamespace("system");
		dataStore.setId("application");
		dataStore.setDataValue(Utils.gson().toJson(Map.of("jsp","application/trip-split")));
		dataStore.setClassName("");

		dataStoreDao.saveDataStore(dataStore).join();


		System.out.println("Complete");
		System.exit(0);
	}
}
