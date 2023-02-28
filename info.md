<headers>
    windows.h = Win API Data types and functions and error codes.
    winevt.h = Event Records for app,sys,security.
    wevtapi.lib = linker which contains compiled code for functions inside the winevt.h.
</headers>

<JNI>
    JNIEXPORT = marks the functions as native functions.
    JNICALL = Calling convections like params to be passed.
    JNIEnv = pointer to use java functions in native code.
    jclass = pointer to a java class obj loaded in the JVM.
</JNI>

<WIN-API>
    LPCWSTR = long pointer to a constant wide string.
    c_str() = const pointer to NULL terminated array.
    ERROR_SUCCESS = 0 (operation completed successfully).
    EVT_HANDLE = handle to windows event obj.


    EvtQuery(Session, Path, Query, flag){
        session {
            NULL => local computer.
            EvtOpenSession() => Remote computers.
        }
        flag = EvtQueryChannelPath => fully specified log path.
    }
    
    EvtNext(handlePointer, arraySize, arrayRef, timeout, flag, returned) {
        timeout = wait for event to be available
        flag = behaviour of the functions {
            0 =  default bahaviour.
            tolerate error = fetch even error occurs.
        }
    }

    EvtCreateRenderContext(valuePathCount, *valuePaths, flags){
        to retrieve all event set valuePathCount to 0 and *valuePaths to NULL.
        flag = behaviour of the functions {
            order in default.
        }
    }

    EvtRender(renderContext, *winObj, flag, *EVT_VARIANT, bufferSizeReq, propertyCount) {
        *EVT_VARIANT = contains the data types reallocate space for this pointer.
    }
</WIN-API>

