#include <string.h>
#include <windows.h>
#include <stdio.h>
#include <winevt.h>
#include <iostream>
#include <string>
#include <vector>
#include <sstream>
#include <jni.h>
#include <fstream>
#include "Main.h"

#pragma comment(lib, "wevtapi.lib")

using namespace std;

#define ARRAY_SIZE 10
#define TIMEOUT 1000 

DWORD PrintResults(JNIEnv *env, EVT_HANDLE hResults, string strpath);
DWORD PrintEvent(JNIEnv *env, EVT_HANDLE hEvent, string strpath); 
string jstring2string(JNIEnv *env, jstring jStr);

JNIEXPORT void JNICALL Java_Main_getEventInfo
(JNIEnv *env, jclass jobj, jstring jpath, jstring jquery) {
	LPCWSTR path;
	string strpath= jstring2string(env,jpath);
	
	wstring temppath = wstring(strpath.begin(), strpath.end());
	path = temppath.c_str();
	string strquery= jstring2string(env,jquery);
	LPCWSTR query;

	wstring tempquery = wstring(strquery.begin(), strquery.end());
	query= tempquery.c_str();
	
    DWORD status = ERROR_SUCCESS;
    EVT_HANDLE hResults = NULL;
    
//  Creates a event query object for the info we provide in the params
//	session, path, query, flags
    hResults = EvtQuery(NULL, path, query, EvtQueryChannelPath);
    if (NULL == hResults)
    {
        status = GetLastError();

        if (ERROR_EVT_CHANNEL_NOT_FOUND == status)
            printf("The channel was not found.\n");
        else if (ERROR_EVT_INVALID_QUERY == status)
            printf("The query is not valid.\n");
        else
            printf("EvtQuery failed with %lu.\n", status);

    }

    PrintResults(env, hResults, strpath);

    if (hResults)
        EvtClose(hResults);
    
    return;

}

DWORD PrintResults(JNIEnv *env, EVT_HANDLE hResults, string strpath){
    DWORD status = ERROR_SUCCESS;
    EVT_HANDLE hEvents[ARRAY_SIZE];
    DWORD dwReturned = 0;
    
//  queryResults, eventArraySize, events, timeout, flags, returned

    while (true)
    {
        if (!EvtNext(hResults, ARRAY_SIZE, hEvents, INFINITE, 0, &dwReturned))
        {
            if (ERROR_NO_MORE_ITEMS != (status = GetLastError()))
            {
                wprintf(L"EvtNext failed with %lu\n", status);
            }
            break;

        }

        for (DWORD i = 0; i < dwReturned; i++)
        {
            if (ERROR_SUCCESS == (status = PrintEvent(env, hEvents[i], strpath)))
            {
                EvtClose(hEvents[i]);
                hEvents[i] = NULL;
            }
            else
            {
                break;
            }
        }
    }

    for (DWORD i = 0; i < dwReturned; i++)
    {
        if (NULL != hEvents[i])
            EvtClose(hEvents[i]);
    }

    return status;
}

DWORD PrintEvent(JNIEnv *env, EVT_HANDLE hEvent, string strpath){
    
    DWORD status = ERROR_SUCCESS;
    EVT_HANDLE hContext = NULL;
    DWORD dwBufferSize = 0;
    DWORD dwBufferUsed = 0;
    DWORD dwPropertyCount = 0;
    PEVT_VARIANT pRenderedValues = NULL;
    ULONGLONG ullTimeStamp = 0;
    ULONGLONG ullNanoseconds = 0;
    SYSTEMTIME st;
    FILETIME ft, ftLocal;

    hContext = EvtCreateRenderContext(0, NULL, EvtRenderContextSystem);
    if (NULL == hContext)
    {
        wprintf(L"EvtCreateRenderContext failed with %lu\n", status = GetLastError());
    }

    if (!EvtRender(hContext, hEvent, EvtRenderEventValues, dwBufferSize, pRenderedValues, &dwBufferUsed, &dwPropertyCount))
    {
        if (ERROR_INSUFFICIENT_BUFFER == (status = GetLastError()))
        {
            dwBufferSize = dwBufferUsed;
            pRenderedValues = (PEVT_VARIANT)malloc(dwBufferSize);
            if (pRenderedValues)
            {
                EvtRender(hContext, hEvent, EvtRenderEventValues, dwBufferSize, pRenderedValues, &dwBufferUsed, &dwPropertyCount);
            }
            else
            {
                wprintf(L"malloc failed\n");
                status = ERROR_OUTOFMEMORY;
            }
        }

        if (ERROR_SUCCESS != (status = GetLastError()))
        {
            wprintf(L"EvtRender failed with %d\n", GetLastError());
        }
    }
    
    DWORD DRecordNumber = pRenderedValues[EvtSystemEventRecordId].UInt64Val;
    string RecordNumber = to_string(DRecordNumber);
    DWORD DEventID = pRenderedValues[EvtSystemEventID].UInt16Val & 0xFFFF;
    string EventId = to_string(DEventID);

    wstring wSource(pRenderedValues[EvtSystemProviderName].StringVal);
    string Source = string(wSource.begin(), wSource.end());

    string ResKeyword;
    uint64_t KeyWords = pRenderedValues[EvtSystemKeywords].UInt64Val;
    string sKeyWords = to_string(KeyWords);
    if (sKeyWords == "9227875636482146304") {
        ResKeyword = "Audit Failure";
    }
    else {
        ResKeyword = "Audit Success";
    }

    wstring wComputerName(pRenderedValues[EvtSystemComputer].StringVal);
    string ComputerName = string(wComputerName.begin(), wComputerName.end());
    
    ullTimeStamp = pRenderedValues[EvtSystemTimeCreated].FileTimeVal;
    ft.dwHighDateTime = (DWORD)((ullTimeStamp >> 32) & 0xFFFFFFFF);
    ft.dwLowDateTime = (DWORD)(ullTimeStamp & 0xFFFFFFFF);
    FileTimeToLocalFileTime(&ft, &ftLocal);
    FileTimeToSystemTime(&ftLocal, &st);    
    ostringstream mon1, day1, year1, hour1, min1, sec1;
    mon1 << st.wMonth; day1 << st.wDay; year1 << st.wYear; hour1 << st.wHour; min1 << st.wMinute; sec1 << st.wSecond;
    string mon = mon1.str(); string day = day1.str(); string year = year1.str(); string hour = hour1.str(); string min = min1.str(); string sec = sec1.str();
    string time_stramp = year + "-" + mon + "-" + day;

    int level;
    if ((EvtVarTypeNull == pRenderedValues[EvtSystemLevel].Type)) {
        level = 0;
    }
    else {
        level = pRenderedValues[EvtSystemLevel].ByteVal;
    }

    string slevel = to_string(level);

    int task;
    if ((EvtVarTypeNull == pRenderedValues[EvtSystemTask].Type)) {
        task = 0;
    }
    else {
        task = pRenderedValues[EvtSystemTask].UInt16Val;
    }

    string stask = to_string(task);
    
    jclass cls_Properties = env->FindClass("java/util/Properties");
    jmethodID mid_Properties_ctor = env->GetMethodID(cls_Properties, "<init>", "()V");
    jmethodID mid_Properties_put = env->GetMethodID(cls_Properties, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jstring key_type = env->NewStringUTF("EventId");
    jstring key_time = env->NewStringUTF("Date");
    jstring key_source = env->NewStringUTF("ResKeyword");
    jstring key_recnum = env->NewStringUTF("RecordNumber");
    jstring key_channel = env->NewStringUTF("Source");
    jstring key_computer = env->NewStringUTF("ComputerName");
    jstring key_level = env->NewStringUTF("Level");
    jstring key_task = env->NewStringUTF("Task");
    
    jstring type = env->NewStringUTF(strpath.c_str());

    jobject prop = env->NewObject(cls_Properties, mid_Properties_ctor);

    env->CallObjectMethod(prop, mid_Properties_put, key_type, env->NewStringUTF(EventId.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_time, env->NewStringUTF(time_stramp.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_source, env->NewStringUTF(ResKeyword.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_recnum, env->NewStringUTF(RecordNumber.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_channel, env->NewStringUTF(Source.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_computer, env->NewStringUTF(ComputerName.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_level, env->NewStringUTF(slevel.c_str()));
    env->CallObjectMethod(prop, mid_Properties_put, key_task, env->NewStringUTF(stask.c_str()));

    jclass myClass = env->FindClass("Main");
    jmethodID mid = env->GetStaticMethodID(myClass, "callback", "(Ljava/util/Properties;Ljava/lang/String;)V");
    env->CallVoidMethod(myClass, mid, prop, type);

    env->DeleteLocalRef(key_type);
    env->DeleteLocalRef(key_time);
    env->DeleteLocalRef(key_source);
    env->DeleteLocalRef(key_recnum);
    env->DeleteLocalRef(key_channel);
    env->DeleteLocalRef(key_computer);
    env->DeleteLocalRef(key_level);
    env->DeleteLocalRef(key_task);
    env->DeleteLocalRef(type);
    env->DeleteLocalRef(prop);
    env->DeleteLocalRef(cls_Properties);

    if (hContext)
        EvtClose(hContext);

    if (pRenderedValues)
        free(pRenderedValues);
    
    return status;
}

string jstring2string(JNIEnv *env, jstring jStr) {
	const char *strChars = env->GetStringUTFChars(jStr, NULL);
    string ret(strChars);
    env->ReleaseStringUTFChars(jStr, strChars);
    return ret;
}
