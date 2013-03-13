package com.yuchs.yuchcaller.sync.task;

import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import javax.microedition.pim.ToDo;
import javax.microedition.pim.ToDoList;

import net.rim.blackberry.api.pdap.BlackBerryToDo;

import com.yuchs.yuchcaller.sync.AbsSync;
import com.yuchs.yuchcaller.sync.AbsSyncData;
import com.yuchs.yuchcaller.sync.SyncMain;

public class TaskSync extends AbsSync {

	public TaskSync(SyncMain syncMain) {
		super(syncMain);
	}

	protected void addPIMItemImpl(PIMList itemList, AbsSyncData d) throws Exception {
		ToDo t = ((ToDoList)itemList).createToDo();
		d.exportData(t);
		
		t.commit();				
		d.setBBID(getPIMItemUID(t));
	}

	protected void deletePIMItemImpl(PIMItem item) throws Exception {
		((ToDoList)item.getPIMList()).removeToDo((ToDo)item);		
	}

	protected String getPIMItemUID(PIMItem item) {
		return AbsSyncData.getStringField(item, BlackBerryToDo.UID);
	}

	protected int getSyncPIMListType() {
		return PIM.TODO_LIST;
	}

	protected AbsSyncData newSyncData() {
		return new TaskSyncData();
	}

}
