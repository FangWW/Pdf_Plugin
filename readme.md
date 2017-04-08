####网上有很多使用pdfium项目做pdf功能,结果加入到自己项目中导致自己的apk大了20M左右,下面按照QQ加载插件的思路做了一个pdf插件


###简单流程
    打开pdf→检查是否安装插件
                        →未安装→下载安装插件→使用插件打开pdf
                        →安装过→使用插件打开pdf
                        


###项目根目录有个123.pdf的测试文件 拷到手机根目录 如下路径

	private String path = "/storage/emulated/0/123.pdf";

###360插件

https://github.com/DroidPluginTeam/DroidPlugin

###pdf项目

https://github.com/barteksc/AndroidPdfViewer

(直接项目打包apk)打成apk插件之前把PDFViewActivity修改获取intent得到文件路径,如下,

       if (getIntent() != null) {
                String key = getIntent().getStringExtra("key");
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(this, "key没有", 1000).show();
                } else {
                    Toast.makeText(this, key, 1000).show();
                    displayFromUri(new File(key));
                }
            }
            
调用插件

		PackageManager pm = mContext.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(item.packageInfo.packageName);
        intent.putExtra("key", mPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ((Activity) mContext).startActivity(intent);

注意
	
	直接打包AndroidPdfViewer项目apk将会有20M左右!
	原因:AndroidPdfViewer中compile 'com.github.barteksc:pdfium-android:1.6.0'的项目中的so库非常大!
	libmodpdfium.so(4M多)
	jnipdfium(100k多)
	然后他又添加了全平台的so库x86_64,x86,mips,arm64-v8a,armeabi,armeabi-v7a
	导致项目20M左右!
	解决办法:
	打包AndroidPdfViewer项目的时候
	配置
	 defaultConfig {
	 	ndk {
            abiFilters "armeabi"//, "armeabi-v7a", "arm64-v8a",x86_64,x86,mips
        }
	  }
	分别过滤只要x86,arm,mips等的6个包
	这时下载插件的是时候只需要下载对应Android手机对应CPU的apk插件就好,就能达到QQ加载pdf插件3M左右大小的水平  
	  
	  


PDFViewActivity完整如下

	@EActivity(R.layout.activity_main)
	@OptionsMenu(R.menu.options)
	public class PDFViewActivity extends AppCompatActivity implements 	OnPageChangeListener, OnLoadCompleteListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;
    public static final int PERMISSION_CODE_1 = 42043;

    public static final String SAMPLE_FILE = "sample.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    @ViewById
    PDFView pdfView;

    @NonConfigurationInstance
    Uri uri;

    @NonConfigurationInstance
    Integer pageNumber = 0;

    String pdfFileName;

    @OptionsItem(R.id.pickFile)
    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }


    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    void afterViews() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE_1
            );
            Toast.makeText(this, "允许后才能查看您的文档哟", 1000).show();
            return;
        }
            if (getIntent() != null) {
                String key = getIntent().getStringExtra("key");
    //            key = "/storage/emulated/0/aaaa.pdf";
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(this, "key没有", 1000).show();
                } else {
                    Toast.makeText(this, key, 1000).show();
                    displayFromUri(new File(key));
                }
            }
    //        if (uri != null) {
    //            displayFromUri(uri);
    //        } else {
    //            displayFromAsset(SAMPLE_FILE);
    //        }
            setTitle(pdfFileName);
        }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    private void displayFromUri(File file) {
        pdfFileName = file.getName();

        pdfView.fromFile(file)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    @OnActivityResult(REQUEST_CODE)
    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        } else if (requestCode == PERMISSION_CODE_1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                afterViews();
            }
        }
    }

	}
