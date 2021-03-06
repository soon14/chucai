package com.sm.sls_app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sm.sls_app.activity.R;
import com.sm.sls_app.dataaccess.SelectedNumbers;
import com.sm.sls_app.dataaccess.utils.HttpUtils;
import com.sm.sls_app.protocol.MD5;
import com.sm.sls_app.protocol.RspBodyBaseBean;
import com.sm.sls_app.ui.adapter.MyBetLotteryListAdapterFC3D;
import com.sm.sls_app.ui.adapter.MyGridViewAdapterFC3D;
import com.sm.sls_app.ui.adapter.ZhuiqiAdapter;
import com.sm.sls_app.ui.adapter.ZhuiqiAdapter.Refresh;
import com.sm.sls_app.utils.App;
import com.sm.sls_app.utils.AppTools;
import com.sm.sls_app.utils.FileUtils;
import com.sm.sls_app.utils.NumberTools;
import com.sm.sls_app.view.ConfirmDialog;
import com.sm.sls_app.view.MyListView2;
import com.sm.sls_app.view.MyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 功能：3D投注页面 版 */
public class BetActivityFC3D extends Activity implements OnClickListener,
		Refresh {

	private final static String TAG = "BetActivityFC3D";

	/* 头部 */
	private RelativeLayout layout_top_select;// 顶部布局
	private ImageButton btn_back; // 返回
	private ImageView iv_up_down;// 玩法提示图标
	private Button btn_playtype;// 玩法
	private ImageButton btn_help;// 帮助
	private ConfirmDialog dialog;// 提示框

	/* 尾部 */
	private Button btn_follow; // 发起合买
	private Button btn_clearall; // 清除全部
	private Button btn_submit; // 付款
	public TextView tv_tatol_count;// 总注数
	public TextView tv_tatol_money;// 总钱数

	private String opt = "11"; // 格式化后的 opt
	private String auth, info, time, imei, crc; // 格式化后的参数
	private LinearLayout btn_continue_select;// 继续投注
	private LinearLayout btn_automatic_select;// 机选1注
	private ImageView bet_btn_deleteall;// 清空
	private TextView bet_tv_guize; // 委托投注规则

	private MyListView2 bet_lv_scheme, bet_lv_zhuiqi;// 自定义listview
	public MyBetLotteryListAdapterFC3D adapter;

	private Intent intent;

	private int type;

	private List<String> list_red, list_blue;

	private List<String> list_bai, list_shi, list_ge, list_hezhi,
			list_zixuanhezhi, list_daxiao, list_jiou;
	private boolean isCaist, isTuolaji;

	private long sumCount, totalMoney; // 方案总注数 // 方案总金额

	private EditText et_bei, et_qi; // 用户输入的倍数 // 用户输入的期数

	private boolean flag;

	private MyHandler myHandler;
	private MyAsynTask myAsynTask;

	private CheckBox bet_cb_stopfollow;
	private int isStopChase = 1;
	private boolean backHome = false;
	private ZhuiqiAdapter zAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 设置无标题
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_bet);
		if (AppTools.beiList == null) {
			AppTools.beiList = new ArrayList<Integer>(AppTools.qi);
		}
		App.activityS.add(this);
		App.activityS1.add(this);
	}

	/** 初始化UI */
	private void findView() {
		myHandler = new MyHandler();
		btn_back = (ImageButton) findViewById(R.id.btn_back);
		btn_playtype = (Button) findViewById(R.id.btn_playtype);
		btn_help = (ImageButton) findViewById(R.id.btn_help);
		iv_up_down = (ImageView) findViewById(R.id.iv_up_down);
		btn_clearall = (Button) findViewById(R.id.btn_clearall);
		btn_submit = (Button) findViewById(R.id.btn_submit);
		btn_continue_select = (LinearLayout) this
				.findViewById(R.id.btn_continue_select);
		btn_automatic_select = (LinearLayout) this
				.findViewById(R.id.btn_automatic_select);
		bet_btn_deleteall = (ImageView) this
				.findViewById(R.id.bet_btn_deleteall);
		btn_follow = (Button) this.findViewById(R.id.btn_follow);
		et_bei = (EditText) this.findViewById(R.id.et_bei);
		et_qi = (EditText) this.findViewById(R.id.et_qi);

		tv_tatol_count = (TextView) this.findViewById(R.id.tv_tatol_count);
		tv_tatol_money = (TextView) this.findViewById(R.id.tv_tatol_money);
		bet_cb_stopfollow = (CheckBox) this
				.findViewById(R.id.bet_cb_stopfollow);
		bet_tv_guize = (TextView) this.findViewById(R.id.bet_tv_guize);

		bet_lv_scheme = (MyListView2) this.findViewById(R.id.bet_lv_scheme);
		bet_lv_zhuiqi = (MyListView2) this.findViewById(R.id.zhuiqi);
		btn_playtype.setText("福彩3D投注单");

		adapter = new MyBetLotteryListAdapterFC3D(BetActivityFC3D.this,
				AppTools.list_numbers);
		zAdapter = new ZhuiqiAdapter(BetActivityFC3D.this,
				AppTools.lottery.getIsuseNameList());
		bet_lv_zhuiqi.setAdapter(zAdapter);
		zAdapter.setListener(this);
		// 隐藏与显示
		btn_help.setVisibility(View.GONE);
		btn_clearall.setVisibility(View.GONE);
		iv_up_down.setVisibility(View.GONE);
		btn_follow.setVisibility(View.VISIBLE);
	}

	/** 初始化属性 */
	private void init() {
		if (MyGridViewAdapterFC3D.playType == 601 && checkVisiBle()) {
			btn_automatic_select.setVisibility(View.VISIBLE);
		} else {
			btn_automatic_select.setVisibility(View.INVISIBLE);
		}
		boolean isFlag = (null != MyGridViewAdapterFC3D.baiSet && MyGridViewAdapterFC3D.baiSet
				.size() != 0)
				|| (null != MyGridViewAdapterFC3D.hezhiSet && MyGridViewAdapterFC3D.hezhiSet
						.size() != 0)
				|| (null != MyGridViewAdapterFC3D.zixuanhezhiSet && MyGridViewAdapterFC3D.zixuanhezhiSet
						.size() != 0)
				|| (null != MyGridViewAdapterFC3D.daxiaoSet && MyGridViewAdapterFC3D.daxiaoSet
						.size() != 0)
				|| (null != MyGridViewAdapterFC3D.jiouSet && MyGridViewAdapterFC3D.jiouSet
						.size() != 0)
				|| MyGridViewAdapterFC3D.isCaist
				|| MyGridViewAdapterFC3D.isTuolaji
				|| (null != MyGridViewAdapterFC3D.shiSet && MyGridViewAdapterFC3D.shiSet
						.size() != 0)
				|| (null != MyGridViewAdapterFC3D.geSet && MyGridViewAdapterFC3D.geSet
						.size() != 0);
		if (isFlag) {

			SelectedNumbers numbers = new SelectedNumbers();

			// 保存彩票号码
			String lotteryNumberHS = "";
			String lotteryNumber = " ";
			if (MyGridViewAdapterFC3D.playType == 610) {// 和数
				for (String st : MyGridViewAdapterFC3D.hezhiSet) {
					lotteryNumber += st + " ";
					lotteryNumberHS += st + ",";
				}
				lotteryNumber = lotteryNumber.substring(0,
						lotteryNumber.length() - 1);
				lotteryNumberHS = lotteryNumberHS.substring(0,
						lotteryNumberHS.length() - 1);
			} else {
				for (String st : MyGridViewAdapterFC3D.baiSet) {
					lotteryNumber += st;
				}
				if (MyGridViewAdapterFC3D.playType == 611) {
					lotteryNumber = lotteryNumber + "," + lotteryNumber;

				}
				if ((MyGridViewAdapterFC3D.playType == 604 && MyGridViewAdapterFC3D.baiSet
						.size() == 0)
						|| (MyGridViewAdapterFC3D.playType == 606 && MyGridViewAdapterFC3D.baiSet
								.size() == 0)) {
					lotteryNumber += "*,";
				} else
					lotteryNumber += ",";
				for (String st : MyGridViewAdapterFC3D.shiSet) {
					lotteryNumber += st;
				}
			}
			if ((MyGridViewAdapterFC3D.playType == 604 && MyGridViewAdapterFC3D.shiSet
					.size() == 0)
					|| (MyGridViewAdapterFC3D.playType == 606 && MyGridViewAdapterFC3D.shiSet
							.size() == 0)) {
				lotteryNumber += "*,";
			} else
				lotteryNumber += ",";
			for (String st : MyGridViewAdapterFC3D.geSet) {
				lotteryNumber += st;
			}
			if ((MyGridViewAdapterFC3D.playType == 604 && MyGridViewAdapterFC3D.geSet
					.size() == 0)
					|| (MyGridViewAdapterFC3D.playType == 606 && MyGridViewAdapterFC3D.geSet
							.size() == 0)) {
				lotteryNumber += "*";
			}
			lotteryNumber = lotteryNumber.trim();
			numbers.setPlayType(MyGridViewAdapterFC3D.playType);
			if (lotteryNumber.contains(",")) {
				String show = lotteryNumber;
				if (lotteryNumber.contains(" ")) {
					show = lotteryNumber.replace(" ", "");
				}
				String[] array = show.split(",");
				StringBuffer num = new StringBuffer();
				if (1 == array.length) {
					numbers.setShowLotteryNumber(lotteryNumber
							.replace("|", " ").replace("*", "-")
							.replace(",", " "));
				} else {
					for (int i = 0; i < array.length; i++) {
						// if(1!=array[i].length()){
						// num.append("("+array[i]+")");
						// }else{
						// num.append(array[i]);
						// }
						if (i == array.length - 1) {
							num.append(array[i]);
						} else {
							num.append(array[i] + " ");
						}
					}
					numbers.setShowLotteryNumber(num.toString()
							.replace("|", " ").replace("*", "-")
							.replace(",", " "));
				}
			}
			if (MyGridViewAdapterFC3D.playType > 601
					&& MyGridViewAdapterFC3D.playType < 604) {
				lotteryNumber = lotteryNumber.replace(",", "");
				char a[] = lotteryNumber.toCharArray();
				String kk = "";
				for (int i = 0; i < a.length; i++) {
					kk = kk + a[i] + ",";
				}
				kk = kk.substring(0, kk.length() - 1);
				kk.trim();
				if (kk.contains("|")) {
					kk = kk.replace("|", ",");// 将“|”替换成“，”
				}
				numbers.setLotteryNumber(kk);
			} else if (MyGridViewAdapterFC3D.playType == 610) {
				String num1 = NumberTools.lotteryNumberFormatConvert(
						numbers.getPlayType(), lotteryNumberHS);
				if (num1.contains("|")) {
					num1 = num1.replace("|", ",");// 将“|”替换成“，”
				}
				numbers.setLotteryNumber(num1);
			} else if (MyGridViewAdapterFC3D.playType == 605
					|| MyGridViewAdapterFC3D.playType == 607
					|| MyGridViewAdapterFC3D.playType == 608) {
				lotteryNumber = lotteryNumber.substring(0,
						lotteryNumber.length() - 2);
				String lotteryNumberNew = "";
				for (int i = 0; i < lotteryNumber.length(); i++) {
					lotteryNumberNew += lotteryNumber.charAt(i) + ",";
				}
				String num1 = lotteryNumberNew.substring(0,
						lotteryNumberNew.length() - 1);
				if (num1.contains("|")) {
					num1 = num1.replace("|", ",");// 将“|”替换成“，”
				}
				numbers.setLotteryNumber(num1);
			} else if (MyGridViewAdapterFC3D.playType == 611) {
				lotteryNumber = lotteryNumber.substring(0,
						lotteryNumber.length() - 1);
				String num1 = lotteryNumber.replace(" ", "").trim();
				if (num1.contains("|")) {
					num1 = num1.replace("|", ",");// 将“|”替换成“，”
				}
				numbers.setLotteryNumber(num1);
			} else {
				String num1 = lotteryNumber.trim();
				if (num1.contains("|")) {
					num1 = num1.replace("|", ",");// 将“|”替换成“，”
				}
				numbers.setLotteryNumber(num1);
			}
			if (MyGridViewAdapterFC3D.playType == 613) {// 猜大小
				lotteryNumber = "";
				for (String st : MyGridViewAdapterFC3D.daxiaoSet) {
					lotteryNumber += st;
				}
				numbers.setShowLotteryNumber(lotteryNumber.trim());
				String num1 = lotteryNumber.trim();
				if (num1.contains("|")) {
					num1 = num1.replace("|", ",");// 将“|”替换成“，”
				}
				numbers.setLotteryNumber(num1);
			}
			if (MyGridViewAdapterFC3D.playType == 614) {// 猜三同
				if (MyGridViewAdapterFC3D.isCaist) {
					numbers.setShowLotteryNumber("三同号");
					numbers.setLotteryNumber("三同号");
				}
			}
			if (MyGridViewAdapterFC3D.playType == 616) {// 猜奇偶
				lotteryNumber = "";
				for (String st : MyGridViewAdapterFC3D.jiouSet) {
					lotteryNumber += st;
				}
				numbers.setShowLotteryNumber(lotteryNumber.trim());
				numbers.setLotteryNumber(lotteryNumber.trim());
			}
			if (MyGridViewAdapterFC3D.playType == 615) {// 拖拉机
				if (MyGridViewAdapterFC3D.isTuolaji) {
					numbers.setShowLotteryNumber("拖拉机");
					numbers.setLotteryNumber("拖拉机");
				}
			}
			numbers.setCount(AppTools.totalCount);
			numbers.setMoney(AppTools.totalCount * 2);
			Log.i(TAG + "-->init()",
					"3D-->ShowLotteryNumber==" + numbers.getShowLotteryNumber());
			Log.i(TAG + "-->init()",
					"3D-->LotteryNumber==" + numbers.getLotteryNumber());
			AppTools.list_numbers.add(0, numbers);
			adapter.notifyDataSetChanged();
		}
		isStopChase = 1;
		changeTextShow();
		btn_submit.setText("付款");
		dialog = new ConfirmDialog(this, R.style.dialog);
	}

	/** 得到玩法ID */
	private int getPlayType(String lotteryId, int playType) {
		if ("6".equals(lotteryId)) {
			switch (playType) {
			case 1:
				playType = 601;
				break;
			case 2:
				playType = 602;
				break;
			case 3:
				playType = 604;
				break;
			case 4:
				playType = 603;
				break;
			case 5:
				playType = 605;
				break;
			}
		} else {
			switch (playType) {
			case 1:
				playType = 6301;
				break;
			case 2:
				playType = 6302;
				break;
			case 3:
				playType = 6304;
				break;
			case 4:
				playType = 6303;
				break;
			case 5:
				playType = 6305;
				break;
			}
		}
		return playType;
	}

	/** 设置监听 */
	private void setListener() {
		btn_continue_select.setOnClickListener(this);
		btn_automatic_select.setOnClickListener(this);
		bet_btn_deleteall.setOnClickListener(this);
		btn_submit.setOnClickListener(this);
		btn_follow.setOnClickListener(this);
		btn_back.setOnClickListener(this);

		bet_lv_scheme.setAdapter(adapter);
		bet_lv_scheme.setOnItemClickListener(new listItemClick());

		et_bei.addTextChangedListener(bei_textWatcher);
		et_qi.addTextChangedListener(qi_textWatcher);
		bet_tv_guize.setOnClickListener(this);

		bet_cb_stopfollow.setOnCheckedChangeListener(new MyCheckChange());
	}

	/** 复选框 **/
	class MyCheckChange implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (isChecked)
				isStopChase = 1;
			else
				isStopChase = 0;
		}
	}

	private boolean checkVisiBle() {
		if (AppTools.list_numbers.size() > 0) {
			for (int i = 0; i < AppTools.list_numbers.size(); i++) {
				if (AppTools.list_numbers.get(i).getPlayType() > 601)
					return false;
			}
		}
		return true;
	}

	/** listView 的 item 点击监听 */
	class listItemClick implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {

			SelectedNumbers num = AppTools.list_numbers.get(position);
			// String type = num.getType();
			// 跳转页面并传值
			intent = new Intent(BetActivityFC3D.this,
					SelectNumberActivityFC3D.class);
			Bundle bundle = new Bundle();
			bundle.putInt("type", num.getPlayType());
			Log.i("x", "listItemClick==>type  " + num.getPlayType());

			AppTools.totalCount = num.getCount();
			Log.i("x",
					"listItemClick==>lotteryNumbers  "
							+ num.getShowLotteryNumber());
			String[] lotteryNumbers = num.getShowLotteryNumber().trim()
					.replace("\\|", " ").split(" ");

			clearSelectNumber();
			if (num.getPlayType() == 610) {
				for (int i = 0; i < lotteryNumbers.length; i++) {
					list_hezhi.add(lotteryNumbers[i]);
				}
			}

			if (lotteryNumbers.length == 3) {
				if (num.getPlayType() == 611) {
					list_bai.add(lotteryNumbers[0]);
					list_shi.add(lotteryNumbers[2]);
				} else {
					for (int i = 0; i < lotteryNumbers.length; i++) {
						for (char str : lotteryNumbers[i].toCharArray()) {
							if (i == 0) {
								list_bai.add(str + "");
							}
							if (i == 1) {
								list_shi.add(str + "");
							}
							if (i == 2) {
								list_ge.add(str + "");
							}
						}
					}
				}

			} else {
				if (lotteryNumbers.length > 0) {

					for (int i = 0; i < lotteryNumbers.length; i++) {
						for (char str : lotteryNumbers[i].toCharArray()) {
							list_bai.add(str + "");
						}
					}

				}
			}
			if (num.getPlayType() == 613)// 猜大小
			{
				if (lotteryNumbers.length > 0) {
					list_daxiao.add(lotteryNumbers[0]);
				}
			}
			if (num.getPlayType() == 616)// 猜奇偶
			{
				if (lotteryNumbers.length > 0) {
					list_jiou.add(lotteryNumbers[0]);
				}
			}
			if (num.getPlayType() == 614) {// 猜三同
				if (lotteryNumbers.length > 0) {
					isCaist = true;
				} else
					isCaist = false;
			}
			if (num.getPlayType() == 615) {// 拖拉机
				if (lotteryNumbers.length > 0) {
					isTuolaji = true;
				} else
					isTuolaji = false;
			}
			bundle.putStringArrayList("bai", (ArrayList<String>) list_bai);
			bundle.putStringArrayList("shi", (ArrayList<String>) list_shi);
			bundle.putStringArrayList("ge", (ArrayList<String>) list_ge);
			bundle.putStringArrayList("hezhi", (ArrayList<String>) list_hezhi);
			bundle.putStringArrayList("zixuanhezhi",
					(ArrayList<String>) list_zixuanhezhi);
			bundle.putStringArrayList("daxiao", (ArrayList<String>) list_daxiao);
			bundle.putStringArrayList("jiou", (ArrayList<String>) list_jiou);
			bundle.putBoolean("santong", isCaist);
			bundle.putBoolean("tuolaji", isTuolaji);
			// 将Bundle 放入Intent
			intent.putExtras(bundle);
			Log.i("x", "listItemClick==>bundle  " + bundle.toString());
			BetActivityFC3D.this.startActivity(intent);
			AppTools.list_numbers.remove(position);

		}

		private void clearSelectNumber() {
			if (null == list_bai) {
				list_bai = new ArrayList<String>();
			} else {
				list_bai.clear();
			}
			if (null == list_shi) {
				list_shi = new ArrayList<String>();
			} else {
				list_shi.clear();
			}
			if (null == list_ge) {
				list_ge = new ArrayList<String>();
			} else {
				list_ge.clear();
			}
			if (null == list_hezhi) {
				list_hezhi = new ArrayList<String>();
			} else {
				list_hezhi.clear();
			}
			if (null == list_zixuanhezhi) {
				list_zixuanhezhi = new ArrayList<String>();
			} else {
				list_zixuanhezhi.clear();
			}
			if (null == list_daxiao) {
				list_daxiao = new ArrayList<String>();
			} else {
				list_daxiao.clear();
			}
			if (null == list_jiou) {
				list_jiou = new ArrayList<String>();
			} else {
				list_jiou.clear();
			}
		}
	}

	/** 当文本的值改变时 */
	private TextWatcher qi_textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void afterTextChanged(Editable edt) {
			if (edt.toString().trim().length() != 0) {
				if (Integer.parseInt(edt.toString().trim()) > AppTools.lottery
						.getDtCanChaseIsuses().size()) {
					et_qi.setText(AppTools.lottery.getDtCanChaseIsuses().size()
							+ "");
					et_qi.setSelection(et_qi.getText().length());
					MyToast.getToast(
							getApplicationContext(),
							"最多可追"
									+ AppTools.lottery.getDtCanChaseIsuses()
											.size() + "期").show();
				}
				if (Integer.parseInt(edt.toString().trim()) == 0) {

					et_qi.setText("1");
					et_qi.setSelection(et_qi.getText().length());
					MyToast.getToast(getApplicationContext(), "至少可追1期").show();
				}
				if (edt.toString().substring(0, 1).equals("0")) {
					et_qi.setText(edt.toString().subSequence(1,
							edt.toString().length()));
					et_qi.setSelection(0);
				}
			}
			changeTextShow();
			setCursorPosition(et_qi);
		}
	};

	/** 当文本的值改变时 */
	private TextWatcher bei_textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable edt) {
			// TODO Auto-generated method stub
			if (edt.toString().trim().length() != 0) {
				if (Integer.parseInt(edt.toString().trim()) > 999) {
					et_bei.setSelection(et_bei.getText().length());
					MyToast.getToast(getApplicationContext(), "最大倍数为999")
							.show();
					et_bei.setText("999");
				}
				if (Integer.parseInt(edt.toString().trim()) == 0) {
					et_bei.setText("1");
					et_bei.setSelection(et_bei.getText().length());
					MyToast.getToast(getApplicationContext(), "最小为1倍").show();
				}
				if (edt.toString().substring(0, 1).equals("0")) {
					et_bei.setText(edt.toString().subSequence(1,
							edt.toString().length()));
					et_bei.setSelection(0);
				}
			}
			changeTextShow();
			setCursorPosition(et_bei);
		}
	};

	public void setCursorPosition(EditText et) {
		CharSequence text = et.getText();
		if (text instanceof Spannable) {
			Selection.setSelection((Spannable) text, text.length());
		}
	}

	/** 公用Button 点击监听 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/** 手选 **/
		case R.id.btn_continue_select:
			btn_handClick();
			break;
		/** 返回 **/
		case R.id.btn_back:
			backToPre();
			break;
		/** 机选 **/
		case R.id.btn_automatic_select:
			btn_randomClick(1);
			break;
		/** 清空 **/
		case R.id.bet_btn_deleteall:
			if (AppTools.list_numbers.size() == 0) {
				MyToast.getToast(BetActivityFC3D.this, "请先选择投注内容").show();
				break;
			}
			dialog.show();
			dialog.setDialogContent("是否清空投注单号码");
			dialog.setDialogResultListener(new ConfirmDialog.DialogResultListener() {

				@Override
				public void getResult(int resultCode) {
					// TODO Auto-generated method stub
					if (1 == resultCode) {// 确定
						AppTools.list_numbers.clear();
						AppTools.beiList.clear();
						adapter.notifyDataSetChanged();
						changeTextShow();
					}
				}
			});
			break;
		/** 付款 **/
		case R.id.btn_submit:
			setEnabled(false);
			myAsynTask = new MyAsynTask();
			myAsynTask.execute();
			break;
		/** 发起合买 **/
		case R.id.btn_follow:
			join();
			break;
		case R.id.bet_tv_guize:
			Intent intent = new Intent(BetActivityFC3D.this,
					PlayDescription.class);
			intent.putExtra("type", 0);
			BetActivityFC3D.this.startActivity(intent);
			break;
		}
	}

	/** 设置是否可用 **/
	private void setEnabled(boolean isEna) {
		btn_submit.setEnabled(isEna);
		bet_btn_deleteall.setEnabled(isEna);
		btn_continue_select.setEnabled(isEna);
		btn_automatic_select.setEnabled(isEna);
		btn_follow.setEnabled(isEna);
		bet_cb_stopfollow.setEnabled(isEna);
		et_bei.setEnabled(isEna);
		et_qi.setEnabled(isEna);
	}

	/** 跳到合买 */
	private void join() {
		// int total = 0; // 总金额
		// for (SelectedNumbers num : AppTools.list_numbers) {
		// total += num.getMoney();
		// }
		// if (total == 0) {
		// MyToast.getToast(getApplicationContext(), "您还没有选择号码").show();
		// return;
		// }
		// total = total * AppTools.bei;
		if (totalMoney == 0) {
			MyToast.getToast(getApplicationContext(), "您还没有选择号码").show();
			return;
		}
		intent = new Intent(BetActivityFC3D.this, JoinActivity.class);
		intent.putExtra("totalMoney", totalMoney);
		intent.putExtra("isStopChase", isStopChase);
		BetActivityFC3D.this.startActivity(intent);
	}

	/** 手选按钮点击事件 */
	private void btn_handClick() {
		clearMyGridViewAdapter();
		Intent intent = new Intent(BetActivityFC3D.this,
				SelectNumberActivityFC3D.class);
		Bundle bundle = new Bundle();
		bundle.putInt("type", MyGridViewAdapterFC3D.playType);
		intent.putExtras(bundle);
		BetActivityFC3D.this.startActivity(intent);
	}

	/**
	 * 清空所有所选信息
	 */
	private void clearMyGridViewAdapter() {
		if (null != MyGridViewAdapterFC3D.baiSet) {
			MyGridViewAdapterFC3D.baiSet.clear();
		}
		if (null != MyGridViewAdapterFC3D.shiSet) {
			MyGridViewAdapterFC3D.shiSet.clear();
		}
		if (null != MyGridViewAdapterFC3D.geSet) {
			MyGridViewAdapterFC3D.geSet.clear();
		}
		if (null != MyGridViewAdapterFC3D.daxiaoSet) {
			MyGridViewAdapterFC3D.daxiaoSet.clear();
		}
		if (null != MyGridViewAdapterFC3D.jiouSet) {
			MyGridViewAdapterFC3D.jiouSet.clear();
		}
		if (null != MyGridViewAdapterFC3D.zixuanhezhiSet) {
			MyGridViewAdapterFC3D.zixuanhezhiSet.clear();
		}
		if (null != MyGridViewAdapterFC3D.hezhiSet) {
			MyGridViewAdapterFC3D.hezhiSet.clear();
		}
		MyGridViewAdapterFC3D.isCaist = false;
		MyGridViewAdapterFC3D.isTuolaji = false;
		AppTools.totalCount = 0;
	}

	/** 机选按钮点击事件 */
	private void btn_randomClick(int count) {
		// 得到红球的随机数
		String lotteryNumber = "";

		System.out.println(MyGridViewAdapterFC3D.playType);
		if (MyGridViewAdapterFC3D.playType == 601) {
			for (int i = 0; i < 3; i++) {
				lotteryNumber += NumberTools.getRandomNum4(1, 10).get(0) + "|";
			}
		} else if (MyGridViewAdapterFC3D.playType == 602
				|| MyGridViewAdapterFC3D.playType == 6302) {
			if (flag) {
				ArrayList<String> list = NumberTools.getRandomNum4(2, 10);

				list.add(list.get(0));

				Collections.sort(list);

				lotteryNumber = (list.toString().replace(" ", ""))
						.replace(",", "").replace("[", "").replace("]", "");
			} else {
				ArrayList<String> list = NumberTools.getRandomNum4(3, 10);

				Collections.sort(list);

				System.out.println(lotteryNumber);

				lotteryNumber = (list.toString().replace(" ", ""))
						.replace(",", " ").replace("[", " ").replace("]", " ");
			}

		} else if (MyGridViewAdapterFC3D.playType == 603
				|| MyGridViewAdapterFC3D.playType == 6303) {

			ArrayList<String> list = NumberTools.getRandomNum4(4, 10);

			Collections.sort(list);

			lotteryNumber = (list.toString().replace(" ", "")).replace(",", "")
					.replace("[", "").replace("]", "");

		} else if (MyGridViewAdapterFC3D.playType == 604
				|| MyGridViewAdapterFC3D.playType == 6304) {

			ArrayList<String> list = NumberTools.getRandomNum4(2, 10);

			Collections.sort(list);

			lotteryNumber = (list.toString().replace(" ", "")).replace(",", "")
					.replace("[", "").replace("]", "");

		}
		lotteryNumber = lotteryNumber.trim();

		SelectedNumbers numbers = new SelectedNumbers();

		numbers.setPlayType(MyGridViewAdapterFC3D.playType);
		if (lotteryNumber.contains("|")) {
			numbers.setShowLotteryNumber(lotteryNumber.replace("|", " ").trim()
					.replace(" ", "#"));
		} else
			numbers.setShowLotteryNumber(lotteryNumber);
		if (MyGridViewAdapterFC3D.playType == 602
				|| MyGridViewAdapterFC3D.playType == 6302) {
			String a[] = lotteryNumber.split(" ");
			String kk = " ";
			for (int i = 0; i < a.length; i++) {
				kk = kk + a[i];
			}
			kk.trim();
			String num = NumberTools.lotteryNumberFormatConvert(
					numbers.getPlayType(), kk);
			if (num.contains("|")) {
				num = num.replace("|", ",");
			}
			numbers.setLotteryNumber(num);
		} else {
			String num = lotteryNumber.substring(0, lotteryNumber.length() - 1);
			if (num.contains("|")) {
				num = num.replace("|", ",");
			}
			numbers.setLotteryNumber(num);
		}
		if (MyGridViewAdapterFC3D.playType == 604
				|| MyGridViewAdapterFC3D.playType == 6304) {
			numbers.setCount(count = 2); // 设置产生随机数时 的 注数
		} else if (MyGridViewAdapterFC3D.playType == 603
				|| MyGridViewAdapterFC3D.playType == 6303) {
			numbers.setCount(count = 4);
		} else
			numbers.setCount(count = 1);
		numbers.setMoney(2 * count);

		AppTools.list_numbers.add(0, numbers);

		// 刷新Adapter
		adapter.notifyDataSetChanged();

		changeTextShow();
	}

	/** 改变文本的值显示出来 */
	public void changeTextShow() {
		if (et_bei.getText().toString().trim().length() == 0)
			AppTools.bei = 1;
		else
			AppTools.bei = Integer.parseInt(et_bei.getText().toString().trim());

		if (et_qi.getText().toString().trim().length() == 0)
			AppTools.qi = 1;
		else
			AppTools.qi = Integer.parseInt(et_qi.getText().toString().trim());
		if (null != AppTools.beiList) {
			AppTools.beiList.clear();
		} else {
			AppTools.beiList = new ArrayList<Integer>();
		}
		for (int i = 0; i < AppTools.qi; i++) {
			AppTools.beiList.add(AppTools.bei);
		}
		sumCount = 0; // 总注数
		for (SelectedNumbers num : AppTools.list_numbers) {
			sumCount += num.getCount();
		}

		if (sumCount != 0) {
			// totalMoney = sumCount * 2 * AppTools.bei * AppTools.qi;
			tv_tatol_count.setText(sumCount + "");
			// tv_tatol_money.setText(totalMoney + "");
			changeMoney();
		} else {
			tv_tatol_count.setText("0");
			tv_tatol_money.setText("0");
		}
		zAdapter.setData(AppTools.qi, sumCount, AppTools.bei);
	}

	/**
	 * 返回
	 */
	public void backToPre() {
		if (AppTools.list_numbers != null && AppTools.list_numbers.size() != 0) {
			dialog.show();
			dialog.setDialogContent("您退出后号码将会清空！");
			dialog.setDialogResultListener(new ConfirmDialog.DialogResultListener() {

				@Override
				public void getResult(int resultCode) {
					// TODO Auto-generated method stub
					if (1 == resultCode) {// 确定
						AppTools.list_numbers.clear();
						AppTools.beiList.clear();
						MyGridViewAdapterFC3D.baiSet.clear();
						MyGridViewAdapterFC3D.shiSet.clear();
						MyGridViewAdapterFC3D.geSet.clear();
						MyGridViewAdapterFC3D.daxiaoSet.clear();
						MyGridViewAdapterFC3D.jiouSet.clear();
						AppTools.totalCount = 0;
						for (int i = 0; i < App.activityS1.size(); i++) {
							App.activityS1.get(i).finish();
						}
					}
				}
			});
		} else {
			AppTools.list_numbers.clear();
			AppTools.beiList.clear();
			MyGridViewAdapterFC3D.baiSet.clear();
			MyGridViewAdapterFC3D.shiSet.clear();
			MyGridViewAdapterFC3D.geSet.clear();
			MyGridViewAdapterFC3D.daxiaoSet.clear();
			MyGridViewAdapterFC3D.jiouSet.clear();
			AppTools.totalCount = 0;
			for (int i = 0; i < App.activityS1.size(); i++) {
				App.activityS1.get(i).finish();
			}
		}
	}

	/** 重写返回键事件 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backToPre();
		}
		return super.onKeyDown(keyCode, event);
	}

	/*** 异步任务 用来后台获取数据 */
	class MyAsynTask extends AsyncTask<Void, Integer, String> {
		String error = "-10001";

		@Override
		protected String doInBackground(Void... params) {
			if (AppTools.list_numbers.size() > 0) {
				time = RspBodyBaseBean.getTime();
				imei = RspBodyBaseBean.getIMEI(getApplicationContext());

				if (AppTools.user != null) {
					String key = MD5.md5(AppTools.user.getUserPass()
							+ AppTools.MD5_key);
					info = RspBodyBaseBean.changeBet_info2(
							AppTools.lottery.getLotteryID(),
							AppTools.lottery.getIsuseId(), AppTools.bei, 1, 1,
							0, 0, "", "", 0, totalMoney / AppTools.qi,
							sumCount, AppTools.qi > 1 ? AppTools.qi : 0,
							AppTools.qi == 1 ? 0 : totalMoney,
							AppTools.list_numbers, isStopChase);

					System.out.println("fc3d====betInfo====" + info);

					crc = RspBodyBaseBean.getCrc(time, imei, key, info,
							AppTools.user.getUid());
					auth = RspBodyBaseBean.getAuth(crc, time, imei,
							AppTools.user.getUid());
					String values[] = { opt, auth, info };
					String result = HttpUtils.doPost(AppTools.names, values,
							AppTools.path);

					if (null == result)
						return "-10001";
					System.out.println("Bet_3D---result:" + result);

					try {
						JSONObject object = new JSONObject(result);

						if ("0".equals(object.optString("error"))) {
							AppTools.user.setBalance(object
									.optDouble("balance"));
							AppTools.user.setFreeze(object.optDouble("freeze"));
							AppTools.schemeId = object.optInt("schemeids");
							AppTools.lottery.setChaseTaskID(object
									.optInt("chasetaskids"));
						}
						error = object.optString("error");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					error = AppTools.ERROR_UNLONGIN + "";
				}
			} else {
				error = AppTools.ERROR_TOTAL + "";
			}
			return error;
		}

		@Override
		protected void onPostExecute(String result) {
			Log.i("x", "发送消息。。。。。");
			myHandler.sendEmptyMessage(Integer.parseInt(result));
			super.onPostExecute(result);
		}
	}

	/** 处理页面显示的 */
	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AppTools.ERROR_SUCCESS:
				AppTools.list_numbers.clear();
				AppTools.beiList.clear();
				AppTools.totalCount = 0;
				SelectNumberActivityFC3D.clearHashSet();
				Intent intent = new Intent(getApplicationContext(),
						PaySuccessActivity.class);
				intent.putExtra("paymoney", totalMoney);
				BetActivityFC3D.this.startActivity(intent);
				break;
			case AppTools.ERROR_MONEY:
				FileUtils.showMoneyLess(BetActivityFC3D.this, totalMoney);
				break;
			/** 尚未登陆 **/
			case AppTools.ERROR_UNLONGIN:
				MyToast.getToast(BetActivityFC3D.this, "请先登陆").show();
				intent = new Intent(BetActivityFC3D.this, LoginActivity.class);
				intent.putExtra("loginType", "bet");
				BetActivityFC3D.this.startActivity(intent);
				break;
			/** 点击付款时 所选注数为 0 **/
			case AppTools.ERROR_TOTAL:
				MyToast.getToast(BetActivityFC3D.this, "请至少选择一注").show();
				intent = new Intent(BetActivityFC3D.this,
						SelectNumberActivityFC3D.class);
				intent.putExtra("loginType", "bet");
				BetActivityFC3D.this.startActivity(intent);
				break;
			default:
				Toast.makeText(BetActivityFC3D.this, "网络异常，购买失败，请重新点击付款购买。",
						Toast.LENGTH_SHORT).show();
				break;
			}
			if (myAsynTask != null
					&& myAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
				myAsynTask.cancel(true); // 如果Task还在运行，则先取消它
			}
			setEnabled(true);
			super.handleMessage(msg);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (!backHome) {
			if (AppTools.list_numbers == null)
				AppTools.list_numbers = new ArrayList<SelectedNumbers>();
			findView();
			setListener();
			changeTextShow();
			init();
		}
		backHome = false;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		backHome = true;
	}

	/**
	 * 改变总金额文本显示
	 */
	private void changeMoney() {
		if (sumCount != 0) {
			if (null != AppTools.beiList) {
				totalMoney = 0;
				for (int i = 0; i < AppTools.beiList.size(); i++) {
					totalMoney += sumCount * 2 * AppTools.beiList.get(i);
				}
			} else {
				totalMoney = sumCount * 2 * AppTools.bei * AppTools.qi;
			}
			tv_tatol_money.setText(totalMoney + "");
		}
	}

	/**
	 * 追期倍投单个倍数改变的回调
	 * 
	 * @param position
	 * @param bei
	 */
	@Override
	public void refreshData() {
		changeMoney();
		System.out.println("refreshData=======" + AppTools.beiList.toString());
	}
}
