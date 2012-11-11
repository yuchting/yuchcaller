package com.yuchs.yuchcaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

public class DbIndex {
	
	/**
	 * debug index error debug inforamtion output interface
	 * @author tzz
	 *
	 */
	public interface DbIndexDebugOut{
		public void debug(String _tag,Exception e);
		public void debug(String _info);
	}
	
	//! main inputStream to read data
	private ByteArrayInputStream	m_mainInputStream = null;
		
	//! the size of data
	private int		m_phoneDataSize = 0;
	private int		m_cellPhoneDataSize = 0;
	
	//! temprary data for read form the main stream
	private PhoneData	m_tmpPhoneData = new PhoneData();	
	private CellPhoneData m_tmpCellPhoneData = new CellPhoneData();
	
	//! carrier list
	private Vector		m_carrierList = new Vector();
	
	//! province list
	private Vector		m_provinceList = new Vector();
	
	//! city list
	private Vector		m_cityList		= new Vector();
	
	//! special number list
	private Vector		m_specialList = new Vector();
	
	//! the version of db index
	private int		m_dbIndexVersion	= 0;
	
	//! interface to debug infor output
	private DbIndexDebugOut		m_debugOutput	= null;
	
	//! languange code to get the coutry information 
	private int		m_localCode			= 0;
	
	/**
	 * initailize the data base index with debugouput interface
	 * @param _debugOutput
	 * @param _localCode	0 means PR.China zh; 1 means others
	 */
	public DbIndex(DbIndexDebugOut _debugOutput,int _localCode){
		m_debugOutput		= _debugOutput;
		m_localCode			= _localCode;
	}
	
	//! output debug information
	private void debugInfo(String _tag,Exception e){
		if(m_debugOutput != null){
			m_debugOutput.debug(_tag,e);
		}
	}

	//! output debug information
	private void debugInfo(String _info){
		if(m_debugOutput != null){
			m_debugOutput.debug(_info);
		}
	}
	
	//! index the phone number
	public synchronized String findPhoneData(String _number){
		
		try{

			if(_number.length() < 11 && _number.charAt(0) != '+'){
				
				String t_special = specialNumber(_number);
				
				if(t_special.length() == 0){
					// prefix search
					//
					if(_number.charAt(0) == '0' && _number.length() >= 3){
						PhoneData t_pd = searchPhoneData(_number);
						if(t_pd != null){
							t_special = composeLocationInfo(t_pd);
						}
					}else{
						CellPhoneData t_cpd = searchCellPhoneData(_number);
						if(t_cpd != null){
							t_special = composeLocationInfo(t_cpd);
						}
					}
				}
				
				return t_special;
				
			}else if(_number.length() >= 11){
				
				String t_countryCode = "";
				
				if(_number.charAt(0) == '+'){
					
					// country code number
					int t_countryCodeIdx = readCountryCodeIndex(_number);
					if(t_countryCodeIdx == -1){
						// unknow country
						return ""; 
					}
					
					if(t_countryCodeIdx != 0){ 
						// NOT PR.China area
						//
						return m_localCode == 0 ? fsm_internalContry[t_countryCodeIdx] : fsm_internalContry_en[t_countryCodeIdx];
					}
					
					_number = _number.substring(3);
					if(_number.length() == 10){
						// fixed phone to add 0
						//
						_number = "0" + _number;
					}
				}
				
				if(_number.charAt(0) == '0'){
					
					// fixed phone
					PhoneData t_pd = searchPhoneData(_number);
					if(t_pd != null){
						return t_countryCode + composeLocationInfo(t_pd);
					}
					
				}else{
					
					// cell phone
					CellPhoneData t_cpd = searchCellPhoneData(_number);
					if(t_cpd != null){
						return t_countryCode + composeLocationInfo(t_cpd);
					}
				}
			}
			
		}catch(Exception e){
			debugInfo("FPD", e);
		}
		
		return  "";
	}
	
	// compose location information by PhoneData
	private String composeLocationInfo(PhoneData _pd){
		
		String t_province	= "";
		String t_city		= "";
		String t_carrier	= "";
		
		if(_pd instanceof CellPhoneData){
			CellPhoneData t_cpd = (CellPhoneData)_pd;
			
			t_province	= (String)m_provinceList.elementAt(t_cpd.m_province);
			t_city		= (String)m_cityList.elementAt(t_cpd.m_city);
			t_carrier	= (String)m_carrierList.elementAt(t_cpd.m_carrier);
		}else{
			t_province	= (String)m_provinceList.elementAt(_pd.m_province);
			t_city		= (String)m_cityList.elementAt(_pd.m_city);
		}
		
		if(t_city.equals(t_province)){
			// Beijing / ShangHai...
			return t_province + t_carrier;
		}
		
		return t_province + t_city + t_carrier;
	}
	
	private int readCountryCodeIndex(String _number){
		
		if(_number.startsWith("+86")){
			// PR.China
			return 0;
		}
		
		int t_returnIdx = -1;
		int t_cmpLen	= 0;
		
		for(int i = 0;i < fsm_internalNumber.length;i++){
			if(_number.startsWith(fsm_internalNumber[i])){
				if(fsm_internalNumber[i].length() > t_cmpLen){
					t_returnIdx 	= i;
					t_cmpLen		= fsm_internalNumber[i].length();
				}				
			}
		}
		
		return t_returnIdx;
	}
	
	private String specialNumber(String _number){
		try{
			int t_searchNumber;
			
			if(_number.length() == 10 && (_number.startsWith("800") || _number.startsWith("400"))){
				t_searchNumber = parse800or400Number(_number);
			}else{
				t_searchNumber = Integer.parseInt(_number);
			}
			SpecialNumber t_sn = (SpecialNumber)binSearch(t_searchNumber, 2);
			if(t_sn != null){
				return t_sn.m_presents;
			}
		}catch(Exception ex){
			debugInfo("SN", ex);
		}
		
		return "";		
	}
	
	//! get the data base version
	public int getVersion(){
		return m_dbIndexVersion;
	}
	
	//! search the phone data
	private PhoneData searchPhoneData(String _cityNumber){
		
		try{
			int t_num4;
			if(_cityNumber.length() >= 4){
				t_num4 = Integer.parseInt(_cityNumber.substring(0, 4));

				PhoneData t_pd = (PhoneData)binSearch(t_num4,0);
				if(t_pd != null){
					return t_pd;
				}
			}
			
			int t_num3 = Integer.parseInt(_cityNumber.substring(0, 3));			
			return (PhoneData)binSearch(t_num3,0);
			
		}catch(Exception ex){
			debugInfo("SPD", ex);
		}
		
		return null;
	}
	
	//! search the cell phone data
	private CellPhoneData searchCellPhoneData(String _cellPhone){
		try{
			
			if(_cellPhone.length() >= 7){
				int t_num7 = Integer.parseInt(_cellPhone.substring(0,7));
				return (CellPhoneData)binSearch(t_num7,1);
			}
			
		}catch(Exception ex){
			debugInfo("SCPD",ex);	
		}
		return null;
	}
	
	//! bineary search 
	private BinSearchNumber binSearch(int _number,int _numerType)throws Exception{
		int t_begin 	= 0;
		int t_end;
		
		switch(_numerType){
		case 0:
			t_end = (m_phoneDataSize - 1);
			break;
		case 1:
			t_end = (m_cellPhoneDataSize - 1);
			break;
		default:
			t_end = m_specialList.size() - 1;
			break;
		}
		
		int t_index;
		
		while(t_begin <= t_end){
			t_index = (t_begin + t_end) / 2;
			
			BinSearchNumber t_pd = (BinSearchNumber)readPhoneData(t_index,_numerType);
			
			int t_cmp = t_pd.Compare(_number);
			
			if(t_cmp < 0){
				t_begin = t_index + 1;
			}else if(t_cmp > 0){
				t_end = t_index - 1;
			}else{
				return t_pd;
			}
		}
		
		return null;
	}
	
	private BinSearchNumber readPhoneData(int _index,int _numerType)throws Exception{
		m_mainInputStream.reset();
		switch(_numerType){
		case 0:
			m_mainInputStream.skip(_index * 7 + 4); // 4 bytes size of PhoneData
			m_tmpPhoneData.Read(m_mainInputStream);
			return (BinSearchNumber)m_tmpPhoneData;
		case 1:
			m_mainInputStream.skip(_index * 10 + m_phoneDataSize * 7 + 8);// 8 bytes size of PhoneData & CellPhoneData
			m_tmpCellPhoneData.Read(m_mainInputStream);
			return (BinSearchNumber)m_tmpCellPhoneData;
		default:
			return (BinSearchNumber)m_specialList.elementAt(_index);		// special number index
		}
	}
		
	/**
	 * read the idx file by InputStream
	 * @param in
	 */
	public synchronized void ReadIdxFile(InputStream in)throws Exception{
		if(in.read() != 'y' || in.read() != 'u' || in.read() != 'c' || in.read() != 'h'){
			throw new Exception("Error YuchCaller Database File Format!");
		}
		
		m_dbIndexVersion = sendReceive.ReadInt(in);
		
		sendReceive.ReadStringVector(in, m_carrierList);
		sendReceive.ReadStringVector(in, m_provinceList);
		sendReceive.ReadStringVector(in, m_cityList);
		
		m_specialList.removeAllElements();
		
		int t_specialNum = sendReceive.ReadInt(in);
		for(int i = 0;i < t_specialNum;i++){
			SpecialNumber t_sn = new SpecialNumber();
			t_sn.Read(in);
			
			m_specialList.addElement(t_sn);
		}
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		try{

			int t_char;
			while((t_char = in.read()) != -1){
				t_os.write(t_char);			
			}
			
			m_mainInputStream = new ByteArrayInputStream(t_os.toByteArray());
			
		}finally{
			t_os.close();
		}
		
		m_phoneDataSize	= sendReceive.ReadInt(m_mainInputStream);
		
		m_mainInputStream.skip(m_phoneDataSize * 7); // size of PhoneData
		
		m_cellPhoneDataSize = sendReceive.ReadInt(m_mainInputStream);
	}
	
	// prase the 800 or 400 number to int;
	public static int parse800or400Number(String _number)throws IllegalArgumentException{
		if(_number.length() != 10){
			throw new IllegalArgumentException("number argument must be 800xxxxxxx or 400xxxxxxx");
		}
		
		if(_number.startsWith("800")){
			_number = _number.substring(3);
			return 0x80000000 | Integer.parseInt(_number);
		}else{
			_number = _number.substring(3);
			return 0xc0000000 | Integer.parseInt(_number);
		}
	}
	
	// export the number string by integer
	public static String export800or400Number(int _number){
		if((_number & 0x80000000) != 0){
			if((_number & 0x40000000) != 0){
				return "400" + Integer.toString((_number & 0x0fffffff));
			}
			
			return "800" + Integer.toString((_number & 0x0fffffff));
		}
		
		return Integer.toString(_number);
	}
	
	private static final String[] fsm_internalNumber =
	{
		"+86",
		"+1",
		"+1264",
		"+1268",
		"+1242",
		"+1246",
		"+1441",
		"+1284",
		"+1345",
		"+1767",
		"+1809",
		"+1473",
		"+1876",
		"+1664",
		"+1787",
		"+1939",
		"+1869",
		"+1758",
		"+1784",
		"+1868",
		"+1649",
		"+1340",
		"+1671",
		"+1670",
		"+20",
		"+210",
		"+211",
		"+212",
		"+213",
		"+216",
		"+218",
		"+220",
		"+221",
		"+222",
		"+223",
		"+224",
		"+225",
		"+226",
		"+227",
		"+228",
		"+229",
		"+230",
		"+231",
		"+232",
		"+233",
		"+234",
		"+235",
		"+236",
		"+237",
		"+238",
		"+239",
		"+240",
		"+241",
		"+242",
		"+243",
		"+244",
		"+245",
		"+246",
		"+247",
		"+248",
		"+249",
		"+250",
		"+251",
		"+252",
		"+253",
		"+254",
		"+255",
		"+256",
		"+257",
		"+258",
		"+259",
		"+260",
		"+261",
		"+262",
		"+263",
		"+264",
		"+265",
		"+266",
		"+267",
		"+268",
		"+269",
		"+27",
		"+290",
		"+291",
		"+295",
		"+297",
		"+298",
		"+299",
		"+30",
		"+31",
		"+32",
		"+33",
		"+34",
		"+350",
		"+351",
		"+352",
		"+353",
		"+354",
		"+355",
		"+356",
		"+357",
		"+358",
		"+359",
		"+36",
		"+37",
		"+370",
		"+371",
		"+372",
		"+373",
		"+374",
		"+375",
		"+376",
		"+377",
		"+378",
		"+379",
		"+38",
		"+380",
		"+381",
		"+382",
		"+384",
		"+385",
		"+386",
		"+387",
		"+388",
		"+389",
		"+39",
		"+40",
		"+41",
		"+42",
		"+420",
		"+421",
		"+423",
		"+43",
		"+44",
		"+45",
		"+46",
		"+47",
		"+48",
		"+49",
		"+500",
		"+501",
		"+502",
		"+503",
		"+504",
		"+505",
		"+506",
		"+507",
		"+508",
		"+509",
		"+51",
		"+52",
		"+53",
		"+54",
		"+55",
		"+56",
		"+57",
		"+58",
		"+590",
		"+591",
		"+592",
		"+593",
		"+594",
		"+595",
		"+596",
		"+597",
		"+598",
		"+599",
		"+60",
		"+61",
		"+62",
		"+63",
		"+64",
		"+65",
		"+66",
		"+670",
		"+671",
		"+672",
		"+673",
		"+674",
		"+675",
		"+676",
		"+677",
		"+678",
		"+679",
		"+680",
		"+681",
		"+682",
		"+683",
		"+684",
		"+685",
		"+686",
		"+687",
		"+688",
		"+689",
		"+690",
		"+691",
		"+692",
		"+7",
		"+800",
		"+808",
		"+81",
		"+82",
		"+84",
		"+850",
		"+851",
		"+852",
		"+853",
		"+854",
		"+855",
		"+856",
		"+870",
		"+875",
		"+876",
		"+877",
		"+878",
		"+879",
		"+880",
		"+881",
		"+882",
		"+886",
		"+90",
		"+91",
		"+92",
		"+93",
		"+94",
		"+95",
		"+960",
		"+961",
		"+962",
		"+963",
		"+964",
		"+965",
		"+966",
		"+967",
		"+968",
		"+969",
		"+970",
		"+971",
		"+972",
		"+973",
		"+974",
		"+975",
		"+976",
		"+977",
		"+98",
		"+992",
		"+993",
		"+994",
		"+995",
		"+996",
		"+998",
	};
	
	private static final String[] fsm_internalContry =
	{		
		"中华人民共和国",
		"美国/加拿大",
		"安圭拉岛",
		"安提瓜和巴布达",
		"巴哈马",
		"巴巴多斯",
		"百慕大",
		"英属维京群岛",
		"开曼群岛",
		"多米尼克",
		"多米尼加共和国",
		"格林纳达",
		"牙买加",
		"蒙特塞拉特",
		"波多黎各",
		"波多黎各",
		"圣基茨和尼维斯",
		"圣卢西亚",
		"圣文森特和格林纳丁斯",
		"特立尼达和多巴哥",
		"特克斯和凯科斯群岛",
		"美属维京群岛",
		"关岛",
		"北马里亚纳群岛",
		"埃及",
		"拟分配西撒哈拉",
		"南苏丹",
		"摩洛哥",
		"阿尔及利亚",
		"突尼斯",
		"利比亚",
		"冈比亚",
		"塞内加尔",
		"毛里塔尼亚",
		"马里",
		"几内亚",
		"科特迪瓦",
		"布基纳法索",
		"尼日尔",
		"多哥",
		"贝宁",
		"毛里求斯",
		"利比里亚",
		"塞拉利昂",
		"加纳",
		"尼日利亚",
		"乍得",
		"中非共和国",
		"喀麦隆",
		"佛得角",
		"圣多美和普林西比",
		"赤道几内亚",
		"加蓬",
		"刚果共和国（布）",
		"刚果民主共和国（金）（即前扎伊尔）",
		"安哥拉",
		"几内亚比绍",
		"迪戈加西亚岛",
		"阿森松岛",
		"塞舌尔",
		"苏丹",
		"卢旺达",
		"埃塞俄比亚",
		"索马里",
		"吉布提",
		"肯尼亚",
		"坦桑尼亚",
		"乌干达",
		"布隆迪",
		"莫桑比克",
		"从未使用――参见255坦桑尼亚",
		"赞比亚",
		"马达加斯加",
		"留尼汪和马约特",
		"津巴布韦",
		"纳米比亚",
		"马拉维",
		"莱索托",
		"博茨瓦纳",
		"斯威士兰",
		"科摩罗",
		"南非",
		"圣赫勒拿",
		"厄立特里亚",
		"中止（原先分配给圣马力诺，参见+378）",
		"阿鲁巴",
		"法罗群岛",
		"格陵兰",
		"希腊",
		"荷兰",
		"比利时",
		"法国",
		"西班牙",
		"直布罗陀",
		"葡萄牙",
		"卢森堡",
		"爱尔兰",
		"冰岛",
		"阿尔巴尼亚",
		"马耳他",
		"塞浦路斯",
		"芬兰",
		"保加利亚",
		"匈牙利",
		"曾经是德意志民主共和国（东德）的区号，合并后的德国区号为49",
		"立陶宛",
		"拉脱维亚",
		"爱沙尼亚",
		"摩尔多瓦",
		"亚美尼亚",
		"白俄罗斯",
		"安道尔",
		"摩纳哥",
		"圣马力诺",
		"保留给梵蒂冈",
		"前南斯拉夫分裂前的区号",
		"乌克兰",
		"塞尔维亚",
		"黑山 (黑山)",
		"拟分配科索沃",
		"克罗地亚",
		"斯洛文尼亚",
		"波黑",
		"欧洲电话号码空间――环欧洲服务",
		"马其顿（前南斯拉夫马其顿共和国, FYROM）",
		"意大利",
		"罗马尼亚",
		"瑞士",
		"曾经是捷克斯洛伐克的区号",
		"捷克",
		"斯洛伐克",
		"列支敦士登",
		"奥地利",
		"英国",
		"丹麦",
		"瑞典",
		"挪威",
		"波兰",
		"德国",
		"福克兰群岛",
		"伯利兹",
		"危地马拉",
		"萨尔瓦多",
		"洪都拉斯",
		"尼加拉瓜",
		"哥斯达黎加",
		"巴拿马",
		"圣皮埃尔和密克隆群岛",
		"海地",
		"秘鲁",
		"墨西哥",
		"古巴（本应属于北美区，由于历史原因分在5区）",
		"阿根廷",
		"巴西",
		"智利",
		"哥伦比亚",
		"委内瑞拉",
		"瓜德罗普",
		"玻利维亚",
		"圭亚那",
		"厄瓜多尔",
		"法属圭亚那",
		"巴拉圭",
		"马提尼克",
		"苏里南",
		"乌拉圭",
		"荷属安的列斯",
		"马来西亚",
		"澳大利亚",
		"印度尼西亚",
		"菲律宾",
		"新西兰",
		"新加坡",
		"泰国",
		"曾经是北马里亚纳群岛（现在是1）",
		"曾经是关岛 (现在是1)",
		"南极洲、圣诞岛、可可斯群岛、和诺福克岛",
		"文莱",
		"瑙鲁",
		"巴布亚新几内亚",
		"汤加",
		"所罗门群岛",
		"瓦努阿图",
		"斐济",
		"帕劳",
		"瓦利斯和富图纳群岛",
		"库克群岛",
		"纽埃",
		"美属萨摩亚",
		"萨摩亚",
		"基里巴斯，吉尔伯特群岛",
		"新喀里多尼亚",
		"图瓦卢，埃利斯群岛",
		"法属波利尼西亚",
		"托克劳群岛",
		"密克罗尼西亚联邦",
		"马绍尔群岛",
		"俄罗斯、哈萨克斯坦",
		"国际免费电话",
		"International Shared Cost Service",
		"日本",
		"大韩民国",
		"越南",
		"朝鲜民主主义人民共和国",
		"测试专用",
		"香港",
		"澳门",
		"总经群岛共和国",
		"柬埔寨",
		"老挝",
		"Inmersat \"SNAC\" 卫星电话",
		"预留给海洋移动通讯服务",
		"预留给海洋移动通讯服务",
		"预留给海洋移动通讯服务",
		"环球个人通讯服务",
		"预留给国家移动/海洋使用",
		"孟加拉人民共和国",
		"移动卫星系统",
		"国际网络",
		"台湾",
		"土耳其",
		"印度",
		"巴基斯坦",
		"阿富汗",
		"斯里兰卡",
		"缅甸",
		"马尔代夫",
		"黎巴嫩",
		"约旦",
		"叙利亚",
		"伊拉克",
		"科威特",
		"沙特阿拉伯",
		"也门",
		"阿曼",
		"曾经是也门民主人民共和国（南也门）的区号，合并后的也门统一使用967区号",
		"预留给巴勒斯坦",
		"阿拉伯联合酋长国",
		"以色列",
		"巴林",
		"卡塔尔",
		"不丹",
		"蒙古",
		"尼泊尔",
		"伊朗",
		"塔吉克斯坦",
		"土库曼斯坦",
		"阿塞拜疆",
		"格鲁吉亚",
		"吉尔吉斯斯坦",
		"乌兹别克斯坦",
	};
	
	private static final String[]  fsm_internalContry_en =
	{
		"People's Republic of China",
		"USA / Canada",
		"Anguilla",
		"Antigua and Barbuda",
		"Bahamas",
		"Barbados",
		"Bermuda",
		"British Virgin Islands",
		"Cayman Islands",
		"Dominique",
		"Dominican Republic",
		"Grenada",
		"Jamaica",
		"Montserrat",
		"Puerto Rico",
		"Puerto Rico",
		"Saint Kitts and Nevis",
		"St. Lucia",
		"Saint Vincent and the Grenadines",
		"Trinidad and Tobago",
		"Turks and Caicos Islands",
		"U.S. Virgin Islands",
		"Guam",
		"Northern Mariana Islands",
		"Egypt",
		"To be allocated for the Western Sahara",
		"South Sudan",
		"Morocco",
		"Algeria",
		"Tunisia",
		"Libya",
		"Gambia",
		"Senegal",
		"Mauritania",
		"Mali",
		"Guinea",
		"Cote d'Ivoire",
		"Burkina Faso",
		"Niger",
		"Togo",
		"Benin",
		"Mauritius",
		"Liberia",
		"Sierra Leone",
		"Ghana",
		"Nigeria",
		"Chad",
		"Central African Republic",
		"Cameroon",
		"Cape Verde",
		"Sao Tome and Principe",
		"Equatorial Guinea",
		"Gabon",
		"Republic of Congo (Brazzaville)",
		"The Democratic Republic of Congo (DRC) (formerly Zaire)",
		"Angola",
		"Guinea-Bissau",
		"The island of Diego Garcia",
		"Ascension",
		"Seychelles",
		"Sudan",
		"Rwanda",
		"Ethiopia",
		"Somalia",
		"Djibouti",
		"Kenya",
		"Tanzania",
		"Uganda",
		"Burundi",
		"Mozambique",
		"Never used - see 255 Tanzania",
		"Zambia",
		"Madagascar",
		"Reunion and Mayotte",
		"Zimbabwe",
		"Namibia",
		"Malawi",
		"Lesotho",
		"Botswana",
		"Swaziland",
		"Comoros",
		"South Africa",
		"St. Helena",
		"Eritrea",
		"Abort (originally assigned to San Marino, see +378)",
		"Aruba",
		"Faroe Islands",
		"Greenland",
		"Greece",
		"Netherlands",
		"Belgium",
		"France",
		"Spain",
		"Gibraltar",
		"Portugal",
		"Luxembourg",
		"Ireland",
		"Iceland",
		"Albania",
		"Malta",
		"Cyprus",
		"Finland",
		"Bulgaria",
		"Hungary",
		"Once the German Democratic Republic (East Germany) area code, the Germany area code combined 49",
		"Lithuania",
		"Latvia",
		"Estonia",
		"Moldova",
		"Armenia",
		"Belarus",
		"Andorra",
		"Monaco",
		"San Marino",
		"Reserved to the Vatican",
		"Former Yugoslavia collapsed in front of the area code",
		"Ukraine",
		"Serbia",
		"Montenegro (Montenegro)",
		"To be allocated to Kosovo.",
		"Croatia",
		"Slovenia",
		"Bosnia and Herzegovina",
		"European phone number space - ring European Service",
		"Macedonia (the former Yugoslav Republic of Macedonia, FYROM)",
		"Italy",
		"Romania",
		"Switzerland",
		"Once the area code of Czechoslovakia",
		"Czech Republic",
		"Slovakia",
		"Liechtenstein",
		"Austria",
		"United Kingdom",
		"Denmark",
		"Sweden",
		"Norway",
		"Poland",
		"Made in Germany",
		"Falkland Islands",
		"Belize",
		"Guatemala",
		"El Salvador",
		"Honduras",
		"Nicaragua",
		"Costa Rica",
		"Panama",
		"Saint Pierre and Miquelon",
		"Haiti",
		"Peru",
		"Mexico",
		"Cuba ",
		"Argentina",
		"Brazil",
		"Chile",
		"Columbia",
		"Venezuela",
		"Guadeloupe",
		"Bolivia",
		"Guyana",
		"Ecuador",
		"French Guiana",
		"Paraguay",
		"Martinique",
		"Suriname",
		"Uruguay",
		"Netherlands Antilles",
		"Malaysia",
		"Australia",
		"Indonesia",
		"Philippines",
		"New Zealand",
		"Singapore",
		"Thailand",
		"Once the Northern Mariana Islands (now 1)",
		"Once Guam (now 1)",
		"Antarctica, Christmas Island, Cocos Islands, and Norfolk Island",
		"Brunei",
		"Nauru",
		"Papua New Guinea",
		"Tonga",
		"Solomon Islands",
		"Vanuatu",
		"Fiji",
		"Palau",
		"Wallis and Futuna",
		"Cook Islands",
		"Niue",
		"American Samoa",
		"Samoa",
		"Kiribati, Gilbert Islands",
		"New Caledonia",
		"Tuvalu, Ellice Islands",
		"French Polynesia",
		"Tokelau Islands",
		"Federated States of Micronesia",
		"Marshall Islands",
		"Russia, Kazakhstan",
		"International Toll Free",
		"International Shared Cost Service",
		"Japan",
		"Republic of Korea",
		"Vietnam",
		"Democratic People's Republic of Korea",
		"Test-specific",
		"Hong Kong",
		"Macao",
		"General Islands Republic",
		"Cambodia",
		"Laos",
		"Inmersat \"SNAC\" satellite phone",
		"Reserved for marine mobile communications services",
		"Reserved for marine mobile communications services",
		"Reserved for marine mobile communications services",
		"Universal Personal Communications Services",
		"Reserved for national mobile / marine use",
		"The People's Republic of Bangladesh",
		"Mobile Satellite Systems",
		"International Network",
		"Taiwan",
		"Turkey",
		"India",
		"Pakistan",
		"Afghanistan",
		"Sri Lanka",
		"Burma",
		"Maldives",
		"Lebanon",
		"Jordan",
		"Syria",
		"Iraq",
		"Kuwait",
		"Saudi Arabia",
		"Yemen",
		"Oman",
		"Once the area code of the People's Democratic Republic of Yemen (South Yemen), combined Yemen unified the 967 area code",
		"Reserved for Palestine",
		"United Arab Emirates",
		"Israel",
		"Bahrain",
		"Qatar",
		"Bhutan",
		"Mongolia",
		"Nepal",
		"Iran",
		"Tajikistan",
		"Turkmenistan",
		"Azerbaijan",
		"Georgia",
		"Kyrgyzstan",
		"Uzbekistan",
	};
	
//	public static void main(String[] _args)throws Exception{
//		
//		assert fsm_internalContry.length == fsm_internalContry_en.length;
//		assert fsm_internalContry_en.length == fsm_internalNumber.length;
//		
//		DbIndex t_dbIdx = new DbIndex(new DbIndexDebugOut() {
//			
//			@Override
//			public void debug(String info) {
//				System.err.println(info);
//				
//			}
//			
//			@Override
//			public void debug(String tag, Exception e) {
//				System.err.println("tag");
//				e.printStackTrace();				
//			}
//		},0);
//		
//		FileInputStream t_file = new FileInputStream("yuchcaller.db");
//		try{
//			GZIPInputStream zip = new GZIPInputStream(t_file);
//			t_dbIdx.ReadIdxFile(zip);
//		}finally{
//			t_file.close();
//		}
//		
//		
//		System.out.println(t_dbIdx.findPhoneData("+1264217442960331"));
//	}
	
}
