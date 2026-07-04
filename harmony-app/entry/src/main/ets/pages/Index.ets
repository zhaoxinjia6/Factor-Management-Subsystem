@Entry
@Component
struct Index {
  @State private backendUrl: string = 'http://10.0.2.2:5173';
  @State private isLoading: boolean = true;
  @State private loadError: string = '';
  @State private showSidebar: boolean = false;
  private webController: WebController = new WebController();

  private navItems: NavItem[] = [
    { label: '因子查询', path: '/workspace/customer/factor-overview' },
    { label: '衍生因子管理', path: '/workspace/customer/derived-factor' },
    { label: '风格因子管理', path: '/workspace/customer/style-factor' },
    { label: '多因子分析', path: '/workspace/customer/multi-factor' }
  ];

  aboutToAppear(): void {
    console.info('FactorApp Index aboutToAppear');
  }

  private injectMobileCss(): void {
    try {
      this.webController.runJavaScript({
        script:
          "var __s=document.createElement('style');" +
          "__s.textContent=" +
          "'.workspace-nav{display:none!important}'+" +
          "'.workspace-header{height:48px!important;min-height:48px!important;padding:0 12px!important}'+" +
          "'.workspace-brand{flex-direction:row!important;align-items:center!important;gap:8px!important}'+" +
          "'.brand-title,.brand-subtitle{display:none!important}'+" +
          "'.brand-mark{width:32px!important;height:32px!important;font-size:16px!important;line-height:32px!important;background:#2F80ED!important;color:#fff!important;border-radius:8px!important;text-align:center!important}'+" +
          "'.workspace-account{gap:4px!important}'+" +
          "'.workspace-account .el-button{width:28px!important;height:28px!important;padding:0!important;font-size:14px!important;border:none!important;background:transparent!important}'+" +
          "'.workspace-account .el-button:hover{background:rgba(0,0,0,0.05)!important}'+" +
          "'.account-chip{font-size:12px!important;padding:2px 8px!important}'+" +
          "'.factor-tree{display:none!important}.factor-content-grid{display:block!important}.factor-main{width:100%!important;max-width:100%!important;padding-left:0!important}.factor-chart{width:100%!important;min-height:240px!important}'+" +
          "'.factor-filter-grid{grid-template-columns:1fr 1fr!important;gap:6px!important}.factor-filter-grid .el-select{width:100%!important}.factor-filter-grid .el-date-editor{width:100%!important}'+" +
          "'.factor-filter-actions{display:flex!important;gap:6px!important}.factor-filter-actions .el-button{flex:1!important;padding:8px 12px!important;font-size:13px!important}'+" +
          "'.el-table .el-table__cell{font-size:12px!important;padding:4px 6px!important}'+" +
          "'.factor-actions{flex-wrap:wrap!important;gap:4px!important}.factor-actions .el-button{font-size:12px!important;padding:6px 10px!important}'+" +
          "'.factor-pagination{flex-direction:column!important;gap:4px!important}.factor-pagination .el-pagination{flex-wrap:wrap!important;justify-content:center!important}'+" +
          "'.panel>.el-dialog{width:100%!important;max-width:100%!important;height:100%!important;max-height:100%!important;margin:0!important;border-radius:0!important;display:flex!important;flex-direction:column!important}'+" +
          "'.panel>.el-dialog .el-dialog__header{padding:10px 12px!important;border-bottom:1px solid #ebeef5!important}'+" +
          "'.panel>.el-dialog .el-dialog__header .el-dialog__title{font-size:16px!important}'+" +
          "'.panel>.el-dialog .el-dialog__header .el-dialog__headerbtn{top:10px!important;right:10px!important}'+" +
          "'.panel>.el-dialog .el-dialog__body{flex:1!important;overflow-y:auto!important;-webkit-overflow-scrolling:touch!important;padding:12px!important}'+" +
          "'.panel>.el-dialog .el-dialog__footer{flex-shrink:0!important;padding:8px 12px!important;display:flex!important;gap:8px!important;border-top:1px solid #ebeef5!important}'+" +
          "'.panel>.el-dialog .el-dialog__footer .el-button{flex:1!important;justify-content:center!important}'+" +
          /* 表单竖向排版 */
          "'.factor-manage-page .el-form{overflow:visible!important}'+" +
          "'.factor-manage-page .el-form-item{display:block!important;margin-bottom:16px!important}'+" +
          "'.factor-manage-page .el-form-item__label{width:auto!important;text-align:left!important;padding:0 0 4px!important;float:none!important;line-height:1.4!important;height:auto!important}'+" +
          "'.factor-manage-page .el-form-item__content{margin-left:0!important}'+" +
          "'.factor-manage-page .el-form-item.is-error .el-form-item__content{margin-left:0!important}'+" +
          "'.factor-manage-page .el-select,.factor-manage-page .el-input,.factor-manage-page .el-textarea{width:100%!important}'+" +
          "'.factor-manage-page .el-input-number{width:100%!important}'+" +
          "'.factor-manage-page .el-input-number .el-input-number__decrease,.factor-manage-page .el-input-number .el-input-number__increase{width:30px!important}'+" +
          "'.factor-manage-page .el-input-number .el-input__inner{padding-left:34px!important;padding-right:34px!important}'+" +
          /* 公式编辑器 */
          "'.factor-manage-page .formula-toolbar{gap:3px!important;padding:4px 6px!important}'+" +
          "'.factor-manage-page .formula-chip{font-size:11px!important;padding:2px 5px!important;margin:1px!important}'+" +
          "'.factor-manage-page .formula-hint{font-size:10px!important}'+" +
          /* 风格因子：el-transfer 竖向排版 */
          "'.factor-manage-page .el-transfer{display:flex!important;flex-direction:column!important;gap:8px!important;width:100%!important}'+" +
          "'.factor-manage-page .el-transfer-panel{width:100%!important;max-height:180px!important;border:1px solid #dcdfe6!important;border-radius:6px!important}'+" +
          "'.factor-manage-page .el-transfer-panel__header{padding:6px 10px!important;font-size:13px!important;background:#f5f7fa!important}'+" +
          "'.factor-manage-page .el-transfer-panel__body{height:auto!important;max-height:130px!important}'+" +
          "'.factor-manage-page .el-transfer-panel__list{height:auto!important;max-height:130px!important}'+" +
          "'.factor-manage-page .el-transfer-panel__item{padding:6px 10px!important}'+" +
          "'.factor-manage-page .el-transfer__buttons{display:none!important}'+" +
          "'.factor-manage-page .el-transfer .el-transfer-panel__filter{padding:6px!important}'+" +
          "'.factor-manage-page .el-transfer .el-transfer-panel__filter .el-input__inner{height:30px!important;font-size:13px!important}'+" +
          /* 权重表格 */
          "'.factor-manage-page .el-dialog .el-table--small{font-size:12px!important}'+" +
          "'.factor-manage-page .el-dialog .el-table--small .el-table__cell{padding:4px 6px!important}'+" +
          "'.factor-manage-page .el-dialog .el-table__body-wrapper{overflow-x:auto!important}'+" +
          "'.factor-manage-page .el-dialog .el-table .el-input-number{width:100%!important}'+" +
          "'.factor-manage-page .el-dialog .el-table .el-input-number .el-input__inner{padding-left:28px!important;padding-right:28px!important}'+" +
          /* card list styles */
          "'.mc-list{padding:0 4px 16px}.mc-card{background:#fff;border-radius:10px;padding:12px 14px;margin-bottom:8px;box-shadow:0 1px 4px rgba(0,0,0,0.06);border:1px solid #f0f0f0}'+" +
          "'.mc-top{display:flex;align-items:center;margin-bottom:6px}.mc-idx{color:#909399;font-size:12px;min-width:28px}.mc-name{font-size:15px;font-weight:600;color:#303133;flex:1}'+" +
          "'.mc-dtl{font-size:12px;color:#606266;line-height:1.6;margin-bottom:8px;padding:6px 8px;background:#f8f9fc;border-radius:6px}'+" +
          "'.mc-act{display:flex;gap:8px;justify-content:flex-end}.mc-act button{padding:4px 12px!important;font-size:12px!important}'+" +
          "'.mc-srch{margin:8px 4px 12px}.mc-srch input{width:100%;height:36px;border:1px solid #dcdfe6;border-radius:8px;padding:0 12px;font-size:14px;box-sizing:border-box;outline:none}.mc-srch input:focus{border-color:#2F80ED}'+" +
          "'.mc-pg{display:flex;align-items:center;justify-content:space-between;padding:12px 4px;gap:8px}.mc-pg .pg-info{font-size:12px;color:#909399}.mc-pg .pg-btns{display:flex;gap:4px}'+" +
          "'.mc-pg .pg-btn{min-width:32px;height:32px;border:1px solid #dcdfe6;border-radius:6px;background:#fff;color:#606266;font-size:13px;display:flex;align-items:center;justify-content:center;padding:0 8px;cursor:pointer}'+" +
          "'.mc-pg .pg-btn.active{background:#2F80ED;color:#fff;border-color:#2F80ED}.mc-pg .pg-btn:disabled{opacity:0.4}'+" +
          "'.panel-head{padding:8px 4px!important}.panel-head h2{font-size:16px!important}.panel-head .el-button{padding:6px 10px!important;font-size:12px!important}'+" +
          /* 隐藏原始表格，保留卡片列表、搜索框和分页 */
          "'.factor-manage-page .el-table{display:none!important}'+" +
          /* 多因子分析页：KPI 横条 + 图表纵向排列，隐藏建模池和效能榜单 */
          "'.multi-factor-page .kpi-row{display:flex!important;flex-direction:column!important;gap:6px!important;padding:8px 12px!important}'+" +
          "'.multi-factor-page .kpi-card{display:flex!important;flex-direction:row!important;align-items:center!important;gap:8px!important;padding:8px 12px!important;border-radius:6px!important}'+" +
          "'.multi-factor-page .kpi-label{font-size:12px!important;color:#606266!important;min-width:70px!important;margin:0!important}'+" +
          "'.multi-factor-page .kpi-value{font-size:16px!important;font-weight:700!important;min-width:60px!important;text-align:right!important}'+" +
          "'.multi-factor-page .kpi-sub{font-size:11px!important;color:#a8abb2!important;flex:1!important;text-align:right!important}'+" +
          "'.multi-factor-page .content-layout{display:block!important}'+" +
          "'.multi-factor-page .model-pool{display:none!important}'+" +
          "'.multi-factor-page .panel-section{display:none!important}'+" +
          "'.multi-factor-page .chart-main{width:100%!important}'+" +
          "'.multi-factor-page .chart-grid{display:flex!important;flex-direction:column!important;gap:4px!important}'+" +
          "'.multi-factor-page .chart-card{padding:4px 8px!important}'+" +
          "'.multi-factor-page .chart-title{font-size:14px!important;text-align:center!important;font-weight:600!important;margin-bottom:4px!important}'+" +
          "'.multi-factor-page .chart-card > div{height:190px!important}'" +
          ";document.head.appendChild(__s);" +
          /* ── 核心转换逻辑 ── */
          "window.__mc=function(page){" +
          "var tbl=page.querySelector('.el-table');if(!tbl||page.querySelector('.mc-list'))return;" +
          "var isD=(page.querySelector('h2')||{}).textContent||'';isD=isD.indexOf('衍生')>=0;" +
          /* 从 DOM 提取数据 + 操作按钮 */
          "var data=[];var trs=tbl.querySelectorAll('tbody tr');" +
          "trs.forEach(function(r,ri){var td=r.querySelectorAll('td');if(td.length<2)return;" +
          "var obj={ri:ri,name:(td[1]&&td[1].textContent)||''};" +
          "if(isD){obj.f=(td[2]&&td[2].textContent)||'';obj.d=(td[3]&&td[3].textContent)||'';obj.t=(td[4]&&td[4].textContent)||''}" +
          "else{obj.d=(td[2]&&td[2].textContent)||'';obj.t=(td[3]&&td[3].textContent)||''}" +
          "obj.btns=[];r.querySelectorAll('.el-button').forEach(function(b){obj.btns.push({el:b,text:b.textContent.trim()})});" +
          "data.push(obj)});" +
          "if(!data.length){setTimeout(function(){window.__mc(page)},1000);return}" +
          /* 构建 UI */
          "var srch=document.createElement('div');srch.className='mc-srch';" +
          "var inp=document.createElement('input');inp.placeholder='搜索因子名称...';" +
          "srch.appendChild(inp);page.appendChild(srch);" +
          "var list=document.createElement('div');list.className='mc-list';page.appendChild(list);" +
          /* 渲染函数 */
          "function rdr(pg,ft){pg=pg||1;ft=ft||'';" +
          "var fd=data.filter(function(d){return!ft||d.name.toLowerCase().indexOf(ft.toLowerCase())>=0});" +
          "var tc=fd.length,tp=Math.max(1,Math.ceil(tc/8));if(pg>tp)pg=tp;var st=(pg-1)*8,ed=Math.min(st+8,tc);" +
          "var h='';for(var i=st;i<ed;i++){var d=fd[i];" +
          "h+='<div class=\"mc-card\"><div class=\"mc-top\"><span class=\"mc-idx\">#'+(i+1)+'</span><span class=\"mc-name\">'+d.name+'</span></div>';" +
          "h+='<div class=\"mc-dtl\">';" +
          "if(isD)h+='<div><b>公式:</b> '+(d.f||'-')+'</div>';" +
          "h+='<div><b>说明:</b> '+(d.d||'-')+'</div><div><b>创建:</b> '+(d.t||'-')+'</div>';" +
          "h+='</div><div class=\"mc-act\">';" +
          "d.btns.forEach(function(b){var n=b.text;if(n=='编辑'||n=='删除'||n=='置顶'){" +
          "h+='<a data-i=\"'+d.ri+'\" data-b=\"'+n+'\" class=\"el-button el-button--default el-button--small\" style=\"'+(n=='删除'?'color:#f56c6c':'')+'\">'+n+'</a>'" +
          "}});h+='</div></div>'}" +
          "list.innerHTML=h;" +
          /* 绑定操作按钮点击事件 */
          "list.querySelectorAll('.mc-act a[data-i]').forEach(function(a){" +
          "a.onclick=function(){var ri=parseInt(this.getAttribute('data-i'));var bn=this.getAttribute('data-b');" +
          "var d=data[ri];if(d){var found=null;d.btns.forEach(function(b){if(b.text===bn)found=b});if(found)found.el.click()}}" +
          "});" +
          /* 分页 */
          "if(tp>1){var pgbar=document.createElement('div');pgbar.className='mc-pg';" +
          "var ph='<span class=\"pg-info\">共'+tc+'条/'+pg+'/'+tp+'页</span><div class=\"pg-btns\">';" +
          "ph+='<a class=\"pg-btn\"'+(pg<=1?'style=\"opacity:0.4\"':' onclick=\"window.__mcGo('+page.__mcId+','+(pg-1)+')\"')+' href=\"javascript:void(0)\">‹</a>';" +
          "for(var pi=Math.max(1,pg-2);pi<=Math.min(tp,pg+2);pi++)ph+='<a class=\"pg-btn'+(pi==pg?' active':'')+'\" onclick=\"window.__mcGo('+page.__mcId+','+pi+')\" href=\"javascript:void(0)\">'+pi+'</a>';" +
          "ph+='<a class=\"pg-btn\"'+(pg>=tp?'style=\"opacity:0.4\"':' onclick=\"window.__mcGo('+page.__mcId+','+(pg+1)+')\"')+' href=\"javascript:void(0)\">›</a>';" +
          "ph+='</div>';pgbar.innerHTML=ph;list.appendChild(pgbar)}" +
          "page.__mcFt=function(){var v=(page.querySelector('.mc-srch input')||{}).value||'';rdr(1,v)}};" +
          "page.__mcId=Date.now()+Math.random();" +
          "window.__mcGo=function(id,pg){var p=document.querySelector('[__mcId=\"'+id+'\"]');if(!p)return;var ft=(p.querySelector('.mc-srch input')||{}).value||'';rdr(pg,ft)};" +
          "page.setAttribute('__mcId',page.__mcId);" +
          "inp.addEventListener('input',function(){rdr(1,this.value)});" +
          "rdr(1)};" +
          "setInterval(function(){document.querySelectorAll('.factor-manage-page:not([__mcDone])').forEach(function(p){p.setAttribute('__mcDone','1');window.__mc(p)})},500)"
      });
    } catch (e) {
      console.error('JS injection failed');
    }
  }

  build() {
    Stack() {
      // ── 主 WebView ──
      Column() {
        if (this.isLoading && !this.loadError) {
          LoadingProgress()
            .color('#2F80ED')
            .width(48)
            .height(48)
            .position({ x: '50%', y: '45%' })
            .translate({ x: -24, y: 0 })
        }

        if (this.loadError) {
          Column() {
            Text('加载失败')
              .fontSize(18)
              .fontColor('#303133')
              .margin({ bottom: 12 })
            Text(this.loadError)
              .fontSize(14)
              .fontColor('#909399')
              .textAlign(TextAlign.Center)
              .margin({ bottom: 24 })
            Button('重新加载')
              .type(ButtonType.Capsule)
              .backgroundColor('#2F80ED')
              .fontColor('#FFFFFF')
              .onClick(() => {
                this.loadError = '';
                this.isLoading = true;
                this.webController.refresh();
              })
          }
          .width('100%')
          .height('100%')
          .justifyContent(FlexAlign.Center)
          .alignItems(HorizontalAlign.Center)
        }

        Web({
          src: this.backendUrl,
          controller: this.webController
        })
          .width('100%')
          .height('100%')
          .javaScriptAccess(true)
          .domStorageAccess(true)
          .geolocationAccess(true)
          .fileAccess(true)
          .cacheMode(CacheMode.None)
          .mixedMode(MixedMode.All)
          .onPageBegin(() => {
            console.info('Web page loading started');
          })
          .onPageEnd(() => {
            this.isLoading = false;
            console.info('Web page loaded');
            this.injectMobileCss();
          })
          .onErrorReceive((event: OnErrorReceiveEvent) => {
            this.isLoading = false;
            this.loadError = '无法连接到服务器 (' + event.error.getErrorCode() + ')';
            console.error('Web error: ' + event.error.getErrorInfo());
          })
          .onHttpErrorReceive((event: OnHttpErrorReceiveEvent) => {
            if (event.response.getResponseCode() === 401) {
              return;
            }
            console.warn('HTTP error: ' + event.response.getResponseCode());
          })
      }
      .width('100%')
      .height('100%')
      .backgroundColor('#F5F7FA')

      // ── 右上角菜单按钮（移到顶边栏下方，避免挡住用户图标） ──
      Row() {
        Text('☰')
          .fontSize(20)
          .fontColor('#333333')
      }
      .width(40)
      .height(40)
      .backgroundColor('#FFFFFF')
      .borderRadius(20)
      .shadow({
        radius: 6,
        color: 'rgba(0, 0, 0, 0.15)',
        offsetX: 0,
        offsetY: 2
      })
      .alignItems(VerticalAlign.Center)
      .justifyContent(FlexAlign.Center)
      .position({ right: 16, top: 56 })
      .onClick(() => {
        this.showSidebar = !this.showSidebar;
      })

      // ── 侧边栏遮罩 ──
      if (this.showSidebar) {
        Column()
          .width('100%')
          .height('100%')
          .backgroundColor('rgba(0, 0, 0, 0.3)')
          .onClick(() => {
            this.showSidebar = false;
          })
      }

      // ── 侧边栏面板 ──
      if (this.showSidebar) {
        Column() {
          Row() {
            Text('导航菜单')
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor('#303133')
            Blank()
            Button() {
              Text('✕')
                .fontSize(18)
                .fontColor('#909399')
            }
            .width(32)
            .height(32)
            .backgroundColor('rgba(0,0,0,0.04)')
            .borderRadius(16)
            .onClick(() => {
              this.showSidebar = false;
            })
          }
          .width('100%')
          .padding({ left: 20, right: 20, top: 20, bottom: 16 })

          Divider()
            .width('100%')
            .color('rgba(0,0,0,0.06)')

          Column() {
            ForEach(this.navItems, (item: NavItem) => {
              Column() {
                Row() {
                  Text(item.label)
                    .fontSize(16)
                    .fontColor('#303133')
                  Blank()
                  Text('›')
                    .fontSize(20)
                    .fontColor('#C0C4CC')
                }
                .width('100%')
                .padding({ left: 20, right: 20, top: 14, bottom: 14 })
                .onClick(() => {
                  this.webController.loadUrl({
                    url: this.backendUrl + item.path
                  });
                  this.showSidebar = false;
                })

                Divider()
                  .width('100%')
                  .color('rgba(0,0,0,0.04)')
              }
            }, (item: NavItem) => item.path)
          }
          .width('100%')
          .padding({ top: 8 })

          Blank()
        }
        .width(280)
        .height('100%')
        .position({ right: 0, top: 0 })
        .backgroundColor('#FFFFFF')
        .shadow({
          radius: 12,
          color: 'rgba(0, 0, 0, 0.25)',
          offsetX: -4,
          offsetY: 0
        })
      }
    }
    .width('100%')
    .height('100%')
  }
}

interface NavItem {
  label: string;
  path: string;
}
