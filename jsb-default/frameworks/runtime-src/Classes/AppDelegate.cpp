/****************************************************************************
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos.com

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated engine source code (the "Software"), a limited,
 worldwide, royalty-free, non-assignable, revocable and non-exclusive license
 to use Cocos Creator solely to develop games on your target platforms. You shall
 not use Cocos Creator software for developing other software or tools that's
 used for developing games. You are not granted to publish, distribute,
 sublicense, and/or sell copies of Cocos Creator.

 The software or tools in this License Agreement are licensed, not sold.
 Xiamen Yaji Software Co., Ltd. reserves all rights not expressly granted to you.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/

#include "AppDelegate.h"

#include "cocos2d.h"

#include "cocos/scripting/js-bindings/manual/jsb_module_register.hpp"
#include "cocos/scripting/js-bindings/manual/jsb_global.h"
#include "cocos/scripting/js-bindings/jswrapper/SeApi.h"
#include "cocos/scripting/js-bindings/event/EventDispatcher.h"
#include "cocos/scripting/js-bindings/manual/jsb_classtype.hpp"

#include "EncryptManager.h"

USING_NS_CC;
using namespace std;

AppDelegate::AppDelegate(int width, int height) : Application("Cocos Game", width, height)
{
}

AppDelegate::~AppDelegate()
{
}

bool AppDelegate::applicationDidFinishLaunching()
{
	unsigned char ivec[] = { 0x2e,0xcc,0x39,0x7c,0x1d,0x54,0xf8,0xb3,0x46,0x72,0xe4,0xb7,0xea,0xf7,0x46,0xf3, };
    unsigned char secret[] = { 0x47,0xc1,0x36,0x1e,0x79,0xc0,0xa1,0x9e,0x58,0x8c,0x90,0xae,0xbd,0x81,0x86,0xd5,0x83,0xf5,0x0e,0x00,0xce,0xbe,0x67,0x0c,0xc0,0x69,0xd7,0x4f,0xc9,0x04,0x75,0x37, };

	EncryptManager::getInstance()->setEncryptEnabled(true,
                                                     cxx17::string_view((char*)secret, sizeof(secret)),
                                                     cxx17::string_view((char*)ivec, sizeof(ivec)),
                                                     EncryptManager::ENCF_SIGNATURE | 5 << 16);	
													 
    se::ScriptEngine* se = se::ScriptEngine::getInstance();

    jsb_set_xxtea_key("f58684e0-349e-4a");
    jsb_init_file_operation_delegate();

#if defined(COCOS2D_DEBUG) && (COCOS2D_DEBUG > 0)
    // Enable debugger here
    jsb_enable_debugger("0.0.0.0", 6086, false);
#endif

    se->setExceptionCallback([](const char* location, const char* message, const char* stack){
        // Send exception information to server like Tencent Bugly.
        cocos2d::log("\nUncaught Exception:\n - location :  %s\n - msg : %s\n - detail : \n      %s\n", location, message, stack);
    });

    jsb_register_all_modules();

    se->start();

    se::AutoHandleScope hs;
    jsb_run_script("jsb-adapter/jsb-builtin.js");
    jsb_run_script("main.js");

    se->addAfterCleanupHook([](){
        JSBClassType::destroy();
    });

    return true;
}

// This function will be called when the app is inactive. When comes a phone call,it's be invoked too
void AppDelegate::onPause()
{
    EventDispatcher::dispatchOnPauseEvent();
}

// this function will be called when the app is active again
void AppDelegate::onResume()
{
    EventDispatcher::dispatchOnResumeEvent();
}
