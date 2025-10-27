import {
  NG_VALUE_ACCESSOR,
  NgControl
} from "./chunk-4ECJ7GUA.js";
import {
  CommonModule
} from "./chunk-A5VIZQDW.js";
import "./chunk-YLHXK2KV.js";
import {
  Component,
  Directive,
  EventEmitter,
  Injectable,
  Injector,
  Input,
  NgModule,
  NgZone,
  Output,
  Renderer2,
  ViewChild,
  forwardRef,
  setClassMetadata,
  ɵɵInheritDefinitionFeature,
  ɵɵNgOnChangesFeature,
  ɵɵProvidersFeature,
  ɵɵdefineComponent,
  ɵɵdefineDirective,
  ɵɵdefineInjectable,
  ɵɵdefineInjector,
  ɵɵdefineNgModule,
  ɵɵdirectiveInject,
  ɵɵelement,
  ɵɵinject,
  ɵɵloadQuery,
  ɵɵqueryRefresh,
  ɵɵviewQuery
} from "./chunk-IWRRCXY5.js";
import "./chunk-RSS3ODKE.js";
import "./chunk-XWLXMCJQ.js";

// node_modules/ngx-captcha/fesm2022/ngx-captcha.mjs
var _c0 = ["captchaWrapperElem"];
var ScriptService = class _ScriptService {
  zone;
  scriptElemId = "ngx-catpcha-script";
  /**
   * Name of the global google recaptcha script
   */
  windowGrecaptcha = "grecaptcha";
  /**
   * Name of enterpise property in the global google recaptcha script
   */
  windowGrecaptchaEnterprise = "enterprise";
  /**
   * Name of the global callback
   */
  windowOnLoadCallbackProperty = "ngx_captcha_onload_callback";
  /**
   * Name of the global callback for enterprise
   */
  windowOnLoadEnterpriseCallbackProperty = "ngx_captcha_onload_enterprise_callback";
  globalDomain = "recaptcha.net";
  defaultDomain = "google.com";
  enterpriseApi = "enterprise.js";
  defaultApi = "api.js";
  constructor(zone) {
    this.zone = zone;
  }
  registerCaptchaScript(config, render, onLoad, language) {
    if (this.grecaptchaScriptLoaded(config.useEnterprise)) {
      if (config.useEnterprise) {
        this.zone.run(() => {
          onLoad(window[this.windowGrecaptcha][this.windowGrecaptchaEnterprise]);
        });
      } else {
        this.zone.run(() => {
          onLoad(window[this.windowGrecaptcha]);
        });
      }
      return;
    }
    if (config.useEnterprise) {
      window[this.getCallbackName(true)] = () => this.zone.run(onLoad.bind(this, window[this.windowGrecaptcha][this.windowGrecaptchaEnterprise]));
    } else {
      window[this.getCallbackName(false)] = () => this.zone.run(onLoad.bind(this, window[this.windowGrecaptcha]));
    }
    const scriptElem = document.createElement("script");
    scriptElem.id = this.scriptElemId;
    scriptElem.innerHTML = "";
    scriptElem.src = this.getCaptchaScriptUrl(config, render, language);
    scriptElem.async = true;
    scriptElem.defer = true;
    document.getElementsByTagName("head")[0].appendChild(scriptElem);
  }
  cleanup() {
    const elem = document.getElementById(this.scriptElemId);
    if (elem) {
      elem.remove();
    }
    window[this.getCallbackName()] = void 0;
    window[this.windowGrecaptcha] = void 0;
  }
  /**
   * Indicates if google recaptcha script is available and ready to be used
   */
  grecaptchaScriptLoaded(useEnterprise) {
    if (!window[this.getCallbackName(useEnterprise)] || !window[this.windowGrecaptcha]) {
      return false;
    } else if (useEnterprise && window[this.windowGrecaptcha][this.windowGrecaptchaEnterprise]) {
      return true;
    } else if (window[this.windowGrecaptcha].execute) {
      return true;
    }
    return false;
  }
  /**
   * Gets global callback name
   * @param useEnterprise Optional flag for enterprise script
   * @private
   */
  getCallbackName(useEnterprise) {
    return useEnterprise ? this.windowOnLoadEnterpriseCallbackProperty : this.windowOnLoadCallbackProperty;
  }
  /**
   * Gets language param used in script url
   */
  getLanguageParam(hl) {
    if (!hl) {
      return "";
    }
    return `&hl=${hl}`;
  }
  /**
   * Url to google api script
   */
  getCaptchaScriptUrl(config, render, language) {
    const domain = config.useGlobalDomain ? this.globalDomain : this.defaultDomain;
    const api = config.useEnterprise ? this.enterpriseApi : this.defaultApi;
    const callback = this.getCallbackName(config.useEnterprise);
    return `https://www.${domain}/recaptcha/${api}?onload=${callback}&render=${render}${this.getLanguageParam(language)}`;
  }
  /** @nocollapse */
  static ɵfac = function ScriptService_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || _ScriptService)(ɵɵinject(NgZone));
  };
  /** @nocollapse */
  static ɵprov = ɵɵdefineInjectable({
    token: _ScriptService,
    factory: _ScriptService.ɵfac,
    providedIn: "root"
  });
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(ScriptService, [{
    type: Injectable,
    args: [{
      providedIn: "root"
    }]
  }], () => [{
    type: NgZone
  }], null);
})();
var BaseReCaptchaComponentDirective = class _BaseReCaptchaComponentDirective {
  renderer;
  zone;
  injector;
  scriptService;
  /**
   * Prefix of the captcha element
   */
  captchaElemPrefix = "ngx_captcha_id_";
  setupCaptcha = true;
  /**
   * Google's site key.
   * You can find this under https://www.google.com/recaptcha
   */
  siteKey;
  /**
   * Indicates if global domain 'recaptcha.net' should be used instead of default domain ('google.com')
   */
  useGlobalDomain = false;
  useEnterprise = false;
  /**
   * Type
   */
  type = "image";
  /**
   * Language code. Auto-detects the user's language if unspecified.
   */
  hl;
  /**
   * Tab index
   */
  tabIndex = 0;
  /**
   * Called when captcha receives successful response.
   * Captcha response token is passed to event.
   */
  success = new EventEmitter();
  /**
   * Called when captcha is loaded. Event receives id of the captcha
   */
  load = new EventEmitter();
  /**
   * Called when captcha is reset.
   */
  reset = new EventEmitter();
  /**
   * Called when captcha is loaded & ready. I.e. when you need to execute captcha on component load.
   */
  ready = new EventEmitter();
  /**
   * Error callback
   */
  error = new EventEmitter();
  /**
   * Expired callback
   */
  expire = new EventEmitter();
  /**
   * Indicates if captcha should be set on load
   */
  setupAfterLoad = false;
  /**
   * Captcha element
   */
  captchaElem;
  /**
   * Id of the captcha elem
   */
  captchaId;
  /**
   * Holds last response value
   */
  currentResponse;
  /**
   * If enabled, captcha will reset after receiving success response. This is useful
   * when invisible captcha need to be resolved multiple times on same page
   */
  resetCaptchaAfterSuccess = false;
  /**
   * Required by ControlValueAccessor
   */
  onChange = (val) => {
  };
  onTouched = (val) => {
  };
  /**
   * Indicates if captcha is loaded
   */
  isLoaded = false;
  /**
   * Reference to global reCaptcha API
   */
  reCaptchaApi;
  /**
   * Id of the DOM element wrapping captcha
   */
  captchaElemId;
  /**
   * Form Control to be enable usage in reactive forms
   */
  control;
  constructor(renderer, zone, injector, scriptService) {
    this.renderer = renderer;
    this.zone = zone;
    this.injector = injector;
    this.scriptService = scriptService;
  }
  ngAfterViewInit() {
    this.control = this.injector.get(NgControl, void 0, {
      optional: true
    })?.control;
  }
  ngAfterViewChecked() {
    if (this.setupCaptcha) {
      this.setupCaptcha = false;
      this.setupComponent();
    }
  }
  ngOnChanges(changes) {
    if (changes && changes.hl) {
      if (!changes.hl.firstChange && changes.hl.currentValue !== changes.hl.previousValue) {
        this.scriptService.cleanup();
      }
    }
    if (changes && changes.useGlobalDomain) {
      if (!changes.useGlobalDomain.firstChange && changes.useGlobalDomain.currentValue !== changes.useGlobalDomain.previousValue) {
        this.scriptService.cleanup();
      }
    }
    this.setupCaptcha = true;
  }
  /**
   * Gets captcha response as per reCaptcha docs
   */
  getResponse() {
    return this.reCaptchaApi.getResponse(this.captchaId);
  }
  /**
   * Gets Id of captcha widget
   */
  getCaptchaId() {
    return this.captchaId;
  }
  /**
   * Resets captcha
   */
  resetCaptcha() {
    this.zone.run(() => {
      this.reCaptchaApi.reset();
      this.onChange(void 0);
      this.onTouched(void 0);
      this.reset.next();
    });
  }
  /**
   * Gets last submitted captcha response
   */
  getCurrentResponse() {
    return this.currentResponse;
  }
  /**
   * Reload captcha. Useful when properties (i.e. theme) changed and captcha need to reflect them
   */
  reloadCaptcha() {
    this.setupComponent();
  }
  ensureCaptchaElem(captchaElemId) {
    const captchaElem = document.getElementById(captchaElemId);
    if (!captchaElem) {
      throw Error(`Captcha element with id '${captchaElemId}' was not found`);
    }
    this.captchaElem = captchaElem;
  }
  /**
   * Responsible for instantiating captcha element
   */
  renderReCaptcha() {
    this.zone.runOutsideAngular(() => {
      setTimeout(() => {
        this.captchaId = this.reCaptchaApi.render(this.captchaElemId, this.getCaptchaProperties());
        this.ready.next();
      }, 0);
    });
  }
  /**
   * Called when captcha receives response
   * @param callback Callback
   */
  handleCallback(callback) {
    this.currentResponse = callback;
    this.success.next(callback);
    this.zone.run(() => {
      this.onChange(callback);
      this.onTouched(callback);
    });
    if (this.resetCaptchaAfterSuccess) {
      this.resetCaptcha();
    }
  }
  getPseudoUniqueNumber() {
    return (/* @__PURE__ */ new Date()).getUTCMilliseconds() + Math.floor(Math.random() * 9999);
  }
  setupComponent() {
    this.captchaSpecificSetup();
    this.createAndSetCaptchaElem();
    this.scriptService.registerCaptchaScript({
      useGlobalDomain: this.useGlobalDomain,
      useEnterprise: this.useEnterprise
    }, "explicit", (grecaptcha) => {
      this.onloadCallback(grecaptcha);
    }, this.hl);
  }
  /**
   * Called when google's recaptcha script is ready
   */
  onloadCallback(grecapcha) {
    this.reCaptchaApi = grecapcha;
    if (!this.reCaptchaApi) {
      throw Error(`ReCaptcha Api was not initialized correctly`);
    }
    this.isLoaded = true;
    this.load.next();
    this.renderReCaptcha();
    if (this.setupAfterLoad) {
      this.setupAfterLoad = false;
      this.setupComponent();
    }
  }
  generateNewElemId() {
    return this.captchaElemPrefix + this.getPseudoUniqueNumber();
  }
  createAndSetCaptchaElem() {
    this.captchaElemId = this.generateNewElemId();
    if (!this.captchaElemId) {
      throw Error(`Captcha elem Id is not set`);
    }
    if (!this.captchaWrapperElem) {
      throw Error(`Captcha DOM element is not initialized`);
    }
    this.captchaWrapperElem.nativeElement.innerHTML = "";
    const newElem = this.renderer.createElement("div");
    newElem.id = this.captchaElemId;
    this.renderer.appendChild(this.captchaWrapperElem.nativeElement, newElem);
    setTimeout(() => {
      if (this.captchaElemId) {
        this.ensureCaptchaElem(this.captchaElemId);
      }
    }, 0);
  }
  /**
   * To be aligned with the ControlValueAccessor interface we need to implement this method
   * However as we don't want to update the recaptcha, this doesn't need to be implemented
   */
  writeValue(obj) {
  }
  /**
   * This method helps us tie together recaptcha and our formControl values
   */
  registerOnChange(fn) {
    this.onChange = fn;
  }
  /**
   * At some point we might be interested whether the user has touched our component
   */
  registerOnTouched(fn) {
    this.onTouched = fn;
  }
  /**
   * Handles error callback
   */
  handleErrorCallback() {
    this.zone.run(() => {
      this.onChange(void 0);
      this.onTouched(void 0);
    });
    this.error.next();
  }
  /**
   * Handles expired callback
   */
  handleExpireCallback() {
    this.expire.next();
    this.resetCaptcha();
  }
  /** @nocollapse */
  static ɵfac = function BaseReCaptchaComponentDirective_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || _BaseReCaptchaComponentDirective)(ɵɵdirectiveInject(Renderer2), ɵɵdirectiveInject(NgZone), ɵɵdirectiveInject(Injector), ɵɵdirectiveInject(ScriptService));
  };
  /** @nocollapse */
  static ɵdir = ɵɵdefineDirective({
    type: _BaseReCaptchaComponentDirective,
    inputs: {
      siteKey: "siteKey",
      useGlobalDomain: "useGlobalDomain",
      useEnterprise: "useEnterprise",
      type: "type",
      hl: "hl",
      tabIndex: "tabIndex"
    },
    outputs: {
      success: "success",
      load: "load",
      reset: "reset",
      ready: "ready",
      error: "error",
      expire: "expire"
    },
    features: [ɵɵNgOnChangesFeature]
  });
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(BaseReCaptchaComponentDirective, [{
    type: Directive
  }], () => [{
    type: Renderer2
  }, {
    type: NgZone
  }, {
    type: Injector
  }, {
    type: ScriptService
  }], {
    siteKey: [{
      type: Input
    }],
    useGlobalDomain: [{
      type: Input
    }],
    useEnterprise: [{
      type: Input
    }],
    type: [{
      type: Input
    }],
    hl: [{
      type: Input
    }],
    tabIndex: [{
      type: Input
    }],
    success: [{
      type: Output
    }],
    load: [{
      type: Output
    }],
    reset: [{
      type: Output
    }],
    ready: [{
      type: Output
    }],
    error: [{
      type: Output
    }],
    expire: [{
      type: Output
    }]
  });
})();
var ReCaptchaType;
(function(ReCaptchaType2) {
  ReCaptchaType2[ReCaptchaType2["InvisibleReCaptcha"] = 0] = "InvisibleReCaptcha";
  ReCaptchaType2[ReCaptchaType2["ReCaptcha2"] = 1] = "ReCaptcha2";
})(ReCaptchaType || (ReCaptchaType = {}));
var InvisibleReCaptchaComponent = class _InvisibleReCaptchaComponent extends BaseReCaptchaComponentDirective {
  renderer;
  zone;
  injector;
  scriptService;
  /**
   * This size representing invisible captcha
   */
  size = "invisible";
  /**
   * Theme
   */
  theme = "light";
  /**
   * Badge
   */
  badge = "bottomright";
  captchaWrapperElem;
  recaptchaType = ReCaptchaType.InvisibleReCaptcha;
  constructor(renderer, zone, injector, scriptService) {
    super(renderer, zone, injector, scriptService);
    this.renderer = renderer;
    this.zone = zone;
    this.injector = injector;
    this.scriptService = scriptService;
  }
  ngOnChanges(changes) {
    super.ngOnChanges(changes);
  }
  /**
   * Programatically invoke the reCAPTCHA check. Used if the invisible reCAPTCHA is on a div instead of a button.
   */
  execute() {
    this.zone.runOutsideAngular(() => this.reCaptchaApi.execute(this.captchaId));
  }
  captchaSpecificSetup() {
  }
  /**
  * Gets reCaptcha properties
  */
  getCaptchaProperties() {
    return {
      "sitekey": this.siteKey,
      "callback": (response) => this.zone.run(() => this.handleCallback(response)),
      "expired-callback": () => this.zone.run(() => this.handleExpireCallback()),
      "error-callback": () => this.zone.run(() => this.handleErrorCallback()),
      "badge": this.badge,
      "type": this.type,
      "tabindex": this.tabIndex,
      "size": this.size,
      "theme": this.theme
    };
  }
  /** @nocollapse */
  static ɵfac = function InvisibleReCaptchaComponent_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || _InvisibleReCaptchaComponent)(ɵɵdirectiveInject(Renderer2), ɵɵdirectiveInject(NgZone), ɵɵdirectiveInject(Injector), ɵɵdirectiveInject(ScriptService));
  };
  /** @nocollapse */
  static ɵcmp = ɵɵdefineComponent({
    type: _InvisibleReCaptchaComponent,
    selectors: [["ngx-invisible-recaptcha"]],
    viewQuery: function InvisibleReCaptchaComponent_Query(rf, ctx) {
      if (rf & 1) {
        ɵɵviewQuery(_c0, 5);
      }
      if (rf & 2) {
        let _t;
        ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.captchaWrapperElem = _t.first);
      }
    },
    inputs: {
      theme: "theme",
      badge: "badge"
    },
    standalone: false,
    features: [ɵɵProvidersFeature([{
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => _InvisibleReCaptchaComponent),
      multi: true
    }]), ɵɵInheritDefinitionFeature, ɵɵNgOnChangesFeature],
    decls: 2,
    vars: 0,
    consts: [["captchaWrapperElem", ""]],
    template: function InvisibleReCaptchaComponent_Template(rf, ctx) {
      if (rf & 1) {
        ɵɵelement(0, "div", null, 0);
      }
    },
    encapsulation: 2
  });
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(InvisibleReCaptchaComponent, [{
    type: Component,
    args: [{
      selector: "ngx-invisible-recaptcha",
      template: `
  <div #captchaWrapperElem></div>`,
      providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => InvisibleReCaptchaComponent),
        multi: true
      }],
      standalone: false
    }]
  }], () => [{
    type: Renderer2
  }, {
    type: NgZone
  }, {
    type: Injector
  }, {
    type: ScriptService
  }], {
    theme: [{
      type: Input
    }],
    badge: [{
      type: Input
    }],
    captchaWrapperElem: [{
      type: ViewChild,
      args: ["captchaWrapperElem", {
        static: false
      }]
    }]
  });
})();
var ReCaptcha2Component = class _ReCaptcha2Component extends BaseReCaptchaComponentDirective {
  renderer;
  zone;
  injector;
  scriptService;
  /**
  * Name of the global expire callback
  */
  windowOnErrorCallbackProperty = "ngx_captcha_error_callback";
  /**
  * Name of the global error callback
  */
  windowOnExpireCallbackProperty = "ngx_captcha_expire_callback";
  /**
   * Theme
   */
  theme = "light";
  /**
  * Size
  */
  size = "normal";
  captchaWrapperElem;
  recaptchaType = ReCaptchaType.ReCaptcha2;
  constructor(renderer, zone, injector, scriptService) {
    super(renderer, zone, injector, scriptService);
    this.renderer = renderer;
    this.zone = zone;
    this.injector = injector;
    this.scriptService = scriptService;
  }
  ngOnChanges(changes) {
    super.ngOnChanges(changes);
  }
  ngOnDestroy() {
    window[this.windowOnErrorCallbackProperty] = {};
    window[this.windowOnExpireCallbackProperty] = {};
  }
  captchaSpecificSetup() {
    this.registerCallbacks();
  }
  /**
   * Gets reCaptcha properties
  */
  getCaptchaProperties() {
    return {
      "sitekey": this.siteKey,
      "callback": (response) => this.zone.run(() => this.handleCallback(response)),
      "expired-callback": () => this.zone.run(() => this.handleExpireCallback()),
      "error-callback": () => this.zone.run(() => this.handleErrorCallback()),
      "theme": this.theme,
      "type": this.type,
      "size": this.size,
      "tabindex": this.tabIndex
    };
  }
  /**
   * Registers global callbacks
  */
  registerCallbacks() {
    window[this.windowOnErrorCallbackProperty] = super.handleErrorCallback.bind(this);
    window[this.windowOnExpireCallbackProperty] = super.handleExpireCallback.bind(this);
  }
  /** @nocollapse */
  static ɵfac = function ReCaptcha2Component_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || _ReCaptcha2Component)(ɵɵdirectiveInject(Renderer2), ɵɵdirectiveInject(NgZone), ɵɵdirectiveInject(Injector), ɵɵdirectiveInject(ScriptService));
  };
  /** @nocollapse */
  static ɵcmp = ɵɵdefineComponent({
    type: _ReCaptcha2Component,
    selectors: [["ngx-recaptcha2"]],
    viewQuery: function ReCaptcha2Component_Query(rf, ctx) {
      if (rf & 1) {
        ɵɵviewQuery(_c0, 5);
      }
      if (rf & 2) {
        let _t;
        ɵɵqueryRefresh(_t = ɵɵloadQuery()) && (ctx.captchaWrapperElem = _t.first);
      }
    },
    inputs: {
      theme: "theme",
      size: "size"
    },
    standalone: false,
    features: [ɵɵProvidersFeature([{
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => _ReCaptcha2Component),
      multi: true
    }]), ɵɵInheritDefinitionFeature, ɵɵNgOnChangesFeature],
    decls: 2,
    vars: 0,
    consts: [["captchaWrapperElem", ""]],
    template: function ReCaptcha2Component_Template(rf, ctx) {
      if (rf & 1) {
        ɵɵelement(0, "div", null, 0);
      }
    },
    encapsulation: 2
  });
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(ReCaptcha2Component, [{
    type: Component,
    args: [{
      selector: "ngx-recaptcha2",
      template: `
  <div #captchaWrapperElem></div>`,
      providers: [{
        provide: NG_VALUE_ACCESSOR,
        useExisting: forwardRef(() => ReCaptcha2Component),
        multi: true
      }],
      standalone: false
    }]
  }], () => [{
    type: Renderer2
  }, {
    type: NgZone
  }, {
    type: Injector
  }, {
    type: ScriptService
  }], {
    theme: [{
      type: Input
    }],
    size: [{
      type: Input
    }],
    captchaWrapperElem: [{
      type: ViewChild,
      args: ["captchaWrapperElem", {
        static: false
      }]
    }]
  });
})();
var ReCaptchaV3Service = class _ReCaptchaV3Service {
  scriptService;
  zone;
  constructor(scriptService, zone) {
    this.scriptService = scriptService;
    this.zone = zone;
  }
  /**
   * Executes reCaptcha v3/Enterprise with given action and passes token via callback. You need to verify
   * this callback in your backend to get meaningful results.
   *
   * For more information see https://developers.google.com/recaptcha/docs/v3
   * For enterprise see https://cloud.google.com/recaptcha-enterprise/docs
   *
   * @param siteKey Site key found in your google admin panel
   * @param action Action to log
   * @param callback Callback function to to handle token
   * @param config Optional configuration like useGlobalDomain to be provided
   * @param errorCallback Optional Callback function to handle errors
   */
  execute(siteKey, action, callback, config, errorCallback) {
    this.executeAsPromise(siteKey, action, config).then(callback).catch((error) => errorCallback ? errorCallback(error) : console.error(error));
  }
  /**
   * Executes reCaptcha v3/Enterprise with given action and returns token via Promise. You need to verify
   * this token in your backend to get meaningful results.
   *
   * For more information see https://developers.google.com/recaptcha/docs/v3
   * For enterprise see https://cloud.google.com/recaptcha-enterprise/docs
   *
   * @param siteKey Site key found in your google admin panel
   * @param action Action to log
   * @param config Optional configuration like useGlobalDomain to be provided
   */
  executeAsPromise(siteKey, action, config) {
    return new Promise((resolve, reject) => {
      const configuration = config || {};
      const onRegister = (grecaptcha) => {
        this.zone.runOutsideAngular(() => {
          try {
            grecaptcha.execute(siteKey, {
              action
            }).then((token) => this.zone.run(() => resolve(token)));
          } catch (error) {
            reject(error);
          }
        });
      };
      this.scriptService.registerCaptchaScript(configuration, siteKey, onRegister);
    });
  }
  /** @nocollapse */
  static ɵfac = function ReCaptchaV3Service_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || _ReCaptchaV3Service)(ɵɵinject(ScriptService), ɵɵinject(NgZone));
  };
  /** @nocollapse */
  static ɵprov = ɵɵdefineInjectable({
    token: _ReCaptchaV3Service,
    factory: _ReCaptchaV3Service.ɵfac,
    providedIn: "root"
  });
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(ReCaptchaV3Service, [{
    type: Injectable,
    args: [{
      providedIn: "root"
    }]
  }], () => [{
    type: ScriptService
  }, {
    type: NgZone
  }], null);
})();
var NgxCaptchaModule = class _NgxCaptchaModule {
  /** @nocollapse */
  static ɵfac = function NgxCaptchaModule_Factory(__ngFactoryType__) {
    return new (__ngFactoryType__ || _NgxCaptchaModule)();
  };
  /** @nocollapse */
  static ɵmod = ɵɵdefineNgModule({
    type: _NgxCaptchaModule,
    declarations: [ReCaptcha2Component, InvisibleReCaptchaComponent],
    imports: [CommonModule],
    exports: [ReCaptcha2Component, InvisibleReCaptchaComponent]
  });
  /** @nocollapse */
  static ɵinj = ɵɵdefineInjector({
    providers: [ScriptService, ReCaptchaV3Service],
    imports: [CommonModule]
  });
};
(() => {
  (typeof ngDevMode === "undefined" || ngDevMode) && setClassMetadata(NgxCaptchaModule, [{
    type: NgModule,
    args: [{
      imports: [CommonModule],
      declarations: [ReCaptcha2Component, InvisibleReCaptchaComponent],
      providers: [ScriptService, ReCaptchaV3Service],
      exports: [ReCaptcha2Component, InvisibleReCaptchaComponent]
    }]
  }], null, null);
})();
export {
  BaseReCaptchaComponentDirective,
  InvisibleReCaptchaComponent,
  NgxCaptchaModule,
  ReCaptcha2Component,
  ReCaptchaType,
  ReCaptchaV3Service,
  ScriptService
};
//# sourceMappingURL=ngx-captcha.js.map
