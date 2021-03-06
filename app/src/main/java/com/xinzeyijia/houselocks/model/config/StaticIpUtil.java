package com.xinzeyijia.houselocks.model.config;

import android.content.ContentResolver;
import android.content.Context;
import android.net.LinkAddress;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class StaticIpUtil {
    Context mContext;

    public StaticIpUtil(Context context) {
        mContext = context;
    }

//    public void getNetworkInformation() {
//        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
//        int ipAddress = mWifiManager.getConnectionInfo().getIpAddress();
//        Constant.IP = intToIp(ipAddress);
//        long getwayIpS = mWifiManager.getDhcpInfo().gateway;
//        Constant.gateway = long2ip(getwayIpS);
//        // 注意要加\\,要不出不来,yeah
//        String[] IPS = Constant.IP.split("\\.");
//        Constant.IP = IPS[0] + "." + IPS[1] + "." + IPS[2] + "." + Constant.IPLast;
//        Constant.isConnectSocket = IPS[0] + "." + IPS[1] + "." + IPS[2] + "." + Constant.IPLast;
//        String zeroIP = "0" + "." + "0" + "." + "0" + "." + Constant.IPLast;
//        String equalIP = IPS[0] + "." + IPS[1] + "." + IPS[2] + "." + IPS[3];
//        if (!Constant.IP.equals(zeroIP) && !Constant.IP.equals(equalIP)) {
//            setIpWithTfiStaticIp(false, Constant.IP, Constant.prefix, Constant.dns1, Constant.gateway);
//        }
//    }

    /**
     * 网关 。
     *
     * @param ip
     * @return
     */
    public String long2ip(long ip) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();



    }




    public static WifiConfiguration CreateWifiInfo(WifiManager wifiManager,String SSID, String Password,
                                            int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID+"\"";

        WifiConfiguration tempConfig =  IsExsits(wifiManager,SSID);
        if (tempConfig != null) {
            boolean c=    wifiManager.removeNetwork(tempConfig.networkId);
            Log.e("创建新的",""+c);
        }

        if (Type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }



    // 判断曾经连接过得WiFi中是否存在指定SSID的WifiConfiguration
    public static WifiConfiguration IsExsits(WifiManager wifiManager,String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            System.out.println("existingConfig" + existingConfig.SSID);
            if (existingConfig.SSID.equals("\"" + SSID+"\"")) {
                return existingConfig;
            }
        }
        return null;
    }



    @SuppressWarnings("unchecked")
    public static void setStaticIpConfiguration(WifiManager manager,
                                                WifiConfiguration config, InetAddress ipAddress, int prefixLength,
                                                InetAddress gateway, InetAddress[] dns)
            throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, NoSuchFieldException, InstantiationException {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue(
                "android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment",
                new String[] { "android.net.IpConfiguration$IpAssignment" },
                new Object[] { ipAssignment });

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");

        Object linkAddress = newInstance("android.net.LinkAddress",
                new Class[] { InetAddress.class, int.class }, new Object[] {
                        ipAddress, prefixLength });
        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        ArrayList<Object> aa = (ArrayList<Object>) getField(staticIpConfig, "dnsServers");
        aa.clear();
        for (int i = 0; i < dns.length; i++)
            aa.add(dns[i]);
        callMethod(config, "setStaticIpConfiguration",
                new String[] { "android.net.StaticIpConfiguration" },
                new Object[] { staticIpConfig });
        System.out.println("conconconm" + config);
        int updateNetwork = manager.updateNetwork(config);
        boolean saveConfiguration = manager.saveConfiguration();
        System.out.println("updateNetwork" + updateNetwork + saveConfiguration);

        System.out.println("ttttttttttt" + "成功");

        int netId = manager.addNetwork(config);
        manager.disableNetwork(netId);
        boolean  flag  = manager.enableNetwork(netId, true);
        Log.e("netId",netId+"");
        Log.e("flag",flag+"");

    }



    private static Object newInstance(String className)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, NoSuchMethodException,
            IllegalArgumentException, InvocationTargetException {
        return newInstance(className, new Class[0], new Object[0]);
    }



    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object newInstance(String className,
                                      Class[] parameterClasses, Object[] parameterValues)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, ClassNotFoundException {
        Class clz = Class.forName(className);
        Constructor constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }



    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getEnumValue(String enumClassName, String enumValue)
            throws ClassNotFoundException {
        Class enumClz = (Class) Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }



    private static void setField(Object object, String fieldName, Object value)
            throws IllegalAccessException, IllegalArgumentException,
            NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }



    private static Object getField(Object object, String fieldName) throws IllegalAccessException, IllegalArgumentException,
            NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(fieldName);
        Object out = field.get(object);
        return out;
    }



    @SuppressWarnings("rawtypes")
    private static void callMethod(Object object, String methodName,
                                   String[] parameterTypes, Object[] parameterValues)
            throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException {
        Class[] parameterClasses = new Class[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        Method method = object.getClass().getDeclaredMethod(methodName,
                parameterClasses);
        method.invoke(object, parameterValues);
    }


    public String intToIp(int ipAddress) {
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));

    }
    // 直接使用set方法调用 可能遇到需要地址转换方法如下：
    public static String int2ip(int ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf((int) (ip & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 8) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 16) & 0xff)));
        sb.append('.');
        sb.append(String.valueOf((int) ((ip >> 24) & 0xff)));
        return sb.toString();
    }
    /**
     * 设置静态ip地址的方法
     */

    public boolean setIpWithTfiStaticIp(boolean dhcp, String ip, int prefix, String dns1, String gateway) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        boolean flag=false;
        if (!wifiManager.isWifiEnabled()) {
            // wifi is disabled
            return flag;
        }
        // get the current wifi configuration
        WifiConfiguration wifiConfig = null;

        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration conf : configuredNetworks) {
                if (conf.networkId == connectionInfo.getNetworkId()) {
                    wifiConfig = conf;
                    break;
                }
            }
        }

        if (wifiConfig == null) {
            // wifi is not connected
            return flag;
        }
        if (Build.VERSION.SDK_INT < 11) { // 如果是android2.x版本的话
            ContentResolver ctRes = mContext.getContentResolver();
            Settings.System.putInt(ctRes,
                    Settings.System.WIFI_USE_STATIC_IP, 1);
            Settings.System.putString(ctRes,
                    Settings.System.WIFI_STATIC_IP, "192.168.0.202");
            flag=true;
            return flag;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // 如果是android3.x版本及以上的话

            try {
                setIpAssignment("STATIC", wifiConfig);
                setIpAddress(InetAddress.getByName(ip), prefix, wifiConfig);
                setGateway(InetAddress.getByName(gateway), wifiConfig);
                setDNS(InetAddress.getByName(dns1), wifiConfig);
                int netId = wifiManager.updateNetwork(wifiConfig);
                boolean result =  netId!= -1; //apply the setting
                if(result){
                    boolean isDisconnected =  wifiManager.disconnect();
                    boolean configSaved = wifiManager.saveConfiguration(); //Save it
                    boolean isEnabled = wifiManager.enableNetwork(wifiConfig.networkId, true);
                    // reconnect with the new static IP
                    boolean isReconnected = wifiManager.reconnect();
                }
             /*   wifiManager.updateNetwork(wifiConfig); // apply the setting
                wifiManager.saveConfiguration(); //Save it*/
                System.out.println("静态ip设置成功！");
                flag=true;
                return flag;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("静态ip设置失败！");
                flag=false;
                return flag;
            }
        } else{//如果是android5.x版本及以上的话
            try {
                Class<?> ipAssignment = wifiConfig.getClass().getMethod("getIpAssignment").invoke(wifiConfig).getClass();
                Object staticConf = wifiConfig.getClass().getMethod("getStaticIpConfiguration").invoke(wifiConfig);

                Log.e("wifiConfig.getClass()",wifiConfig.getClass().toString());

                if (dhcp) {
                    wifiConfig.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConfig, Enum.valueOf((Class<Enum>) ipAssignment, "DHCP"));
                    if (staticConf != null) {
                        staticConf.getClass().getMethod("clear").invoke(staticConf);
                        Log.e("自动分配模式","staticConf!=null");
                    }else{
                        Log.e("自动分配模式","staticConf==null");
                    }
                } else {
                    wifiConfig.getClass().getMethod("setIpAssignment", ipAssignment).invoke(wifiConfig, Enum.valueOf((Class<Enum>) ipAssignment, "STATIC"));
                    if (staticConf == null) {
                        Log.e("静态IP模式","staticConf==null");
                        Class<?> staticConfigClass = Class.forName("android.net.StaticIpConfiguration");
                        staticConf = staticConfigClass.newInstance();
                        if (staticConf == null) {
                            Log.e("静态IP模式","staticConf还是==null");
                        }


                    }else{
                        Log.e("静态IP模式","staticConf！=null");

                    }
                    // STATIC IP AND MASK PREFIX
                    Constructor<?> laConstructor = LinkAddress.class.getConstructor(InetAddress.class, int.class);
                    LinkAddress linkAddress = (LinkAddress) laConstructor.newInstance(
                            InetAddress.getByName(ip),
                            prefix);
                    staticConf.getClass().getField("ipAddress").set(staticConf, linkAddress);
                    // GATEWAY
                    staticConf.getClass().getField("gateway").set(staticConf, InetAddress.getByName(gateway));
                    // DNS
                    List<InetAddress> dnsServers = (List<InetAddress>) staticConf.getClass().getField("dnsServers").get(staticConf);
                    dnsServers.clear();
                    dnsServers.add(InetAddress.getByName(dns1));
//                    dnsServers.add(InetAddress.getByName(Constant.dns2)); // Google DNS as DNS2 for safety
                    // apply the new static configuration
                    wifiConfig.getClass().getMethod("setStaticIpConfiguration", staticConf.getClass()).invoke(wifiConfig, staticConf);
                }
                // apply the configuration change
                boolean result = wifiManager.updateNetwork(wifiConfig) != -1; //apply the setting
                Log.e("result",result+"");

                if (result) result = wifiManager.saveConfiguration(); //Save it

                Log.e("saveConfiguration",result+"");

                if (result) wifiManager.reassociate(); // reconnect with the new static IP

                Log.e("reassociate",result+"");


                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disableNetwork(netId);
                flag  = wifiManager.enableNetwork(netId, true);
                Log.e("netId",netId+"");


//                                            if(b){
//                                                Toast.makeText(getApplication(),"连接成功！",Toast.LENGTH_SHORT).show();
//                                            }else{
//                                                Toast.makeText(getApplication(),"连接失败！请确定服务器热点是否开启！",Toast.LENGTH_SHORT).show();
//
//                                            }
//                flag=true;


            } catch (Exception e) {
                e.printStackTrace();
                flag=false;
            }

        }
        return flag;
    }




    private static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException {
        setEnumField(wifiConf, assign, "ipAssignment");
    }


    private static void setIpAddress(InetAddress addr, int prefixLength,
                                     WifiConfiguration wifiConf) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException,
            ClassNotFoundException, InstantiationException,
            InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        Class<?> laClass = Class.forName("android.net.LinkAddress");
        Constructor<?> laConstructor = laClass.getConstructor(new Class[]{

                InetAddress.class, int.class});
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);
        ArrayList<Object> mLinkAddresses = (ArrayList<Object>) getDeclaredField(
                linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }



    private static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }


    private static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class<Enum>) f.getType(), value));
    }


    private static void setGateway(InetAddress gateway, WifiConfiguration wifiConf)
            throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException,
            InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        if (android.os.Build.VERSION.SDK_INT >= 14) { // android4.x版本
            Class<?> routeInfoClass = Class.forName("android.net.RouteInfo");
            Constructor<?> routeInfoConstructor = routeInfoClass
                    .getConstructor(new Class[]{InetAddress.class});
            Object routeInfo = routeInfoConstructor.newInstance(gateway);
            ArrayList<Object> mRoutes = (ArrayList<Object>) getDeclaredField(

                    linkProperties, "mRoutes");
            mRoutes.clear();
            mRoutes.add(routeInfo);
        } else { // android3.x版本
            ArrayList<InetAddress> mGateways = (ArrayList<InetAddress>) getDeclaredField(

                    linkProperties, "mGateways");
            //    mGateways.clear();
            mGateways.add(gateway);

        }
    }


    private static void setDNS(InetAddress dns, WifiConfiguration wifiConf)

            throws SecurityException, IllegalArgumentException,

            NoSuchFieldException, IllegalAccessException {

        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null)
            return;
        ArrayList<InetAddress> mDnses = (ArrayList<InetAddress>)
                getDeclaredField(linkProperties, "mDnses");
        mDnses.clear(); // 清除原有DNS设置（如果只想增加，不想清除，词句可省略）
        mDnses.add(dns);
        //增加新的DNS



    }
}
