package com.yuchs.yuchcaller.sync.contact;

import javax.microedition.pim.PIM;

import net.rim.blackberry.api.pdap.PIMListListener;

import com.yuchs.yuchcaller.sync.AbsSync;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class ContactSync extends AbsSync implements PIMListListener{

	public ContactSync(SyncMain syncMain) {
		super(syncMain);
		
	}

	protected int getSyncPIMListType() {
		return PIM.CONTACT_LIST;
	}

	protected AbsSyncData newSyncData() {
		return new ContactSyncData();
	}
}
