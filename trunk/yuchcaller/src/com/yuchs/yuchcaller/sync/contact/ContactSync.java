package com.yuchs.yuchcaller.sync.contact;

import java.util.Vector;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;

import net.rim.blackberry.api.pdap.BlackBerryPIMList;
import net.rim.blackberry.api.pdap.PIMListListener;

import com.yuchs.yuchcaller.sync.AbsSync;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class ContactSync extends AbsSync implements PIMListListener{

	public ContactSync(SyncMain syncMain) {
		super(syncMain);
		
	}

	protected void addPIMItem(Vector addList) {
		// TODO Auto-generated method stub

	}

	protected void deletePIMItem(Vector delList) {
		// TODO Auto-generated method stub

	}

	protected int getSyncPIMListType() {
		return PIM.CONTACT_LIST;
	}

	protected AbsSyncData newSyncData() {
		return new ContactSyncData();
	}

	protected void updatePIMItem(Vector updateList) {
		// TODO Auto-generated method stub
	}

}
