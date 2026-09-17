# workspace notes - screens/api stuff

checking this against `dev` now. some of the earlier notes were from `main`, which has screens that haven't reached `dev` yet. use the code on this branch when we start each screen.

base - https://prod-api.osebo.ai

main api  = `data/ApiService.kt`.


# activity screens first

- `SplashActivity` - local checks only. decides where to go using prefs/token.
- `LoginActivity`
  - `POST api/v1/auth/signin` (request phone/email OTP)
  - `POST api/v1/auth/signin/password`
  - `POST api/v1/auth/verify-2fa`
  - `POST api/v1/auth/resend-otp`
- `SignUpActivity` (file is SignupActivity.kt)
  - `POST api/v1/auth/signin` to request OTP
  - `POST api/v1/auth/verify-2fa`
  - `POST api/v1/auth/signup`
- `OtpVerificationActivity`
  - `POST api/v1/auth/verify-2fa`
  - `POST api/v1/auth/resend-otp`
- `SelectAccountActivity`
  - `POST api/v1/auth/select-account`
- `ShopCreationActivity` (file name is shop_creation.kt)
  - `GET api/v1/shop-type`
  - `POST api/v1/shops`
  - shop types have a hardcoded fallback if request fails
- `ShopsActivity` - local Room list via `ShopViewModel.loadShops()`. no GET here. chooses a cached shop or routes to creation/main.
- `MainActivity` - app/nav shell. on startup it refreshes shops with `GET api/v1/shops`. logout in this activity only clears local data (doesn't call logout endpoint).

manifest count = 8 activities.

# main nav graph screens

`nav_graph.xml` is the one inflated by `activity_main.xml`. 41 declarations in it (40 fragment tags + one dialog tag).

# dashboard / reporting

- `mainDashboardFragment` -> `MainDashboardFragment` (XML UI on `dev`)
  - `GET api/v1/shops`
  - `GET api/v1/analytics/shop-summary` (loops over shops with each shop id)
  - also refreshes dashboard data through `DashboardRepository`
- `shopDashboardFragment` -> `ShopDashboardFragment` (old XML UI)
  - same 4 analytics refresh calls: shop-summary, time-series, top-stock-items, shop-financial-statement
- `reportsFragment` -> `ReportsFragment`
  - `GET api/v1/analytics/sales-comparison?period=...`
  - `GET api/v1/analytics/shop-summary`
- `shopStatisticsFragment` -> `subpage.ShopStatisticsFragment`
  - mock numbers/charts/products from `StatisticsViewModel`. no endpoint right now.

# shop screens

- `shopsFragment` -> `ShopsFragment`
  - list load is local Room only
  - delete action: `DELETE shops/{id}` (note: unversioned path)
  - setting active shop is local prefs
- `shopDetailsFragment` -> `ShopDetailsFragment`
  - details come from nav args + cached shop list
  - delete: `DELETE shops/{id}`
  - edit/statistics methods in this class currently just toast; nav actions exist in XML but those methods don't use them
- `editShopFragment` -> `EditShopFragment`
  - save is placeholder/local success toast. no API.
  - `PUT shops/{id}` exists in ApiService but this screen doesn't call it.
- `shopBillingFragment` -> `ShopbillingFragment`
  - uses the Shop passed in args. no request; buttons only navigate to plans.

# sales / checkout

- `salesFragment` -> `SalesFragment`
  - sales list/totals are Room data
  - top products does call `GET api/v1/analytics/top-stock-items`
  - filter/export/refresh mostly work on the local list
- `newSaleFragment` -> `NewSaleFragment`
  - `GET api/v1/stock-item`
  - `GET api/v1/customer`
  - barcode first checks Room, then fallback `GET api/v1/stock-item/search/barcode?barcode=...`
  - ordinary text search filters cached/all-products data; it does not use the declared `/search?q=` call here
- `paymentFragment` -> `PaymentFragment`
  - `POST api/v1/sale`
- `receiptFragment` -> `ReceiptFragment`
  - local nav args, PDF/share/email/printing. no API.

# inventory / transfers / suppliers

- `inventoryFragment` -> `InventoryFragment`
  - `GET api/v1/stock-item` on refresh/sync
- `addProductFragment` -> `AddProductFragment`
  - create mode saves to Room only (the multipart `POST api/v1/stock-item/single` is not wired)
  - edit mode can call `PUT api/v1/stock/{productId}` while online
- `productDetailsFragment` -> `ProductDetailsFragment`
  - details from Room
  - restock/update can call `PUT api/v1/stock/{productId}`
  - delete is Room only; declared DELETE endpoint isn't used
- `restockFragment` -> `RestockFragment`
  - `GET api/v1/stock-item`
  - `PUT api/v1/stock/{productId}` for selected product updates
- transfer list/create screens are on `main`, not on this `dev` branch yet. no transfer destinations in this branch's main nav graph.
- `suppliersFragment` -> `SuppliersFragment`
  - hardcoded sample suppliers. add/edit/delete are toast/local only. no API.

# finance bits

- `financeFragment` -> `FinanceFragment`
  - `GET api/v1/general-ledger` (period/startDate/endDate params)
  - `GET api/v1/expense`
  - delete expense path can call `DELETE api/v1/expense/{expenseId}`
- `transactionsFragment` -> `TransactionsFragment`
  - `GET api/v1/general-ledger`
  - `GET api/v1/expense`
  - screen combines both lists. transaction delete/export are local/stub, not an API delete.
- `addExpenseFragment` -> `AddExpenseFragment`
  - `GET api/v1/expense-category`
  - `POST api/v1/expense`
- `expenseCategoriesFragment` -> `ExpenseCategoriesFragment`
  - `GET api/v1/expense-category`
  - `POST api/v1/expense-category`
  - `PUT api/v1/expense-category/{categoryId}`
  - `DELETE api/v1/expense-category/{categoryId}`
- `financialStatementFragment` -> `FinancialStatementFragment`
  - `GET api/v1/general-ledger`
  - chart/time series is generated mock data. despite the name, this screen does not call analytics/shop-financial-statement.
- `financeSettingsFragment` -> `FinanceSettingsFragment`
  - this is really an export-format/period screen right now
  - export loads `GET api/v1/general-ledger`
  - declared `api/v1/shops/{shopId}/settings` GET/PUT calls are not used here

# employees / customers / roles

- `employeesFragment` -> `EmployeesFragment`
  - `GET api/v1/users`
  - attendance/schedule/payroll/performance/import/export are placeholders
  - there is an unused debug method that probes random dev-api employee paths; it is not called
- `customersFragment` -> `CustomersFragment`
  - `GET api/v1/customer` (ViewModel refreshes on init)
  - add: `POST api/v1/customer`
  - search is Room
  - edit/delete UI says coming soon, even though repository methods for PUT/DELETE exist
- `userRolesFragment` -> `UserRolesFragment`
  - hardcoded Manager/Staff roles and permission groups. save waits then shows success. no API.
- `accountFragment` -> `AccountFragment`
  - `GET users/profile`
  - `PUT users/profile`
  - `PUT users/password`
  - account security/deactivation endpoints exist in another unused ViewModel, not this screen
- `sessionsFragment` -> `SessionsFragment`
  - 3 hardcoded sessions + delays. terminate is in-memory. no API.

# contact / faq / ai

- `contactUsFragment` -> `ContactUsFragment`
  - contact info is hardcoded and send-message just reports success. no API.
- `faqFragment` is wrongly mapped to `FaqDetailDialogFragment`, not the real `FaqFragment`
  - static args/UI, no API
- `faq_detail_dialog` also maps to `FaqDetailDialogFragment` (same class a second time)
  - no API
- AI hub/chat screens are on `main`, not on this `dev` branch yet.

# subscription/billing (there are too many versions of this flow)

- `subscriptionsFragment` -> `SubscriptionFragment`
  - `GET api/v1/subscription/shop`
  - `GET api/v1/package`
  - subscribe/trial: `POST api/v1/subscription`
  - cancel: `PATCH api/v1/subscription/{subscriptionId}/cancel`
  - mobile money polling: `GET api/v1/subscription/payment/{paymentId}`
  - also creates a ViewPager containing SubscriptionOverview + the real SubscriptionHistory screen, so opening this container can trigger their calls too
- `subscriptionOverviewFragment` -> `SubscriptionOverviewFragment`
  - `GET api/v1/subscription/shop/active`
  - `GET api/v1/package`
  - checkout/trial: `POST api/v1/subscription`
  - renew: `POST api/v1/subscription/renew`
  - cancel renewal: `PATCH api/v1/subscription/{subscriptionId}/cancel`
  - create/renew may start `GET api/v1/subscription/payment/{paymentId}` polling
- `subscriptionPackagesFragment` -> `SubscriptionPackagesFragment`
  - `GET api/v1/package`
  - trial/payment start: `POST api/v1/subscription`
  - payment flow can poll `GET api/v1/subscription/payment/{paymentId}`
- `subscriptionPaymentFragment` -> `SubscriptionPaymentFragment`
  - `POST api/v1/subscription`
  - then payment polling through `GET api/v1/subscription/payment/{paymentId}`
- `paymentDialogFragment` -> `PaymentDialogFragment` (the one actual `<dialog>` destination)
  - opening it = no call
  - confirm normally calls `POST api/v1/subscription`; when host sets a renewal listener it calls `POST api/v1/subscription/renew` instead
  - create/renew can start payment polling
- `paymentStatusFragment` -> `PaymentStatusFragment`
  - `GET api/v1/subscription/payment/{paymentId}` repeatedly/manual retry
  - after success also checks `GET api/v1/subscription/shop/active` and `GET api/v1/subscription/shop`
  - refreshes shops via `GET api/v1/shops`
- `subscriptionDetailsFragment` -> `SubscriptionDetailsFragment`
  - with subscription id: `GET api/v1/subscription/{subscriptionId}`
  - otherwise by shop: `GET api/v1/subscription/shop`
  - cancel: `PATCH api/v1/subscription/{subscriptionId}/cancel`, followed by shop subscription reload
- `subscriptionManagementFragment` -> `SubscriptionManagementFragment`
  - `GET api/v1/subscription/shop`
  - cancel: `PATCH api/v1/subscription/{subscriptionId}/cancel`
  - registered, but there is no incoming nav action in the main graph
- `paymentHistoryFragment` -> `Fragmentpaymenthistory`
  - bare generated layout stub, no endpoint. this is NOT the working history list.
- `usageStatisticsFragment`
  - graph points to `com.devbrian.osebo.fragments.UsageStatisticsFragment`, which does not exist. actual file/class is `FragmentUsageStatistics` and it is also only a blank layout stub.

# screens/classes implemented outside the main graph

these still matter for compose inventory because some are shown manually or nested.

- `AddEmployeeDialogFragment` - shown from Employees. `POST api/v1/users`.
- `AddRoleDialogFragment` - `POST roles` (unversioned path, creates its own ApiClient).
- `AddExpenseCategoryDialogFragment` - collects data only; host ExpenseCategories screen performs POST.
- `EditExpenseCategoryDialogFragment` - collects data only; host performs PUT.
- `BarcodeScannerFragment` - camera/local callback, no API. New Sale does the barcode API fallback after callback.
- `FilterDialogFragment` - local filter fields only.
- `EditAccountDialogFragment` - local dialog only, no save API.
- `PermissionsDialogFragment` - generated placeholder Fragment, no API.
- `dialogs.PaymentMethodDialogFragment` - local choice callback, no API.
- real `FaqFragment` - hardcoded FAQ list, no API; graph accidentally points at FaqDetailDialog instead.
- `SubscriptionHistoryFragment` - this is the working history page nested by `SubscriptionPagerAdapter`
  - `GET api/v1/subscription/shop`
  - `GET api/v1/subscription/payments?page=...&limit=...&provider=...`
- `subpage.ShopOverviewFragment` - displays Shop args only.
- `subpages.ShopSubscriptionFragment`
  - `GET api/v1/subscription/shop`
  - `PATCH api/v1/subscription/{subscriptionId}/cancel`
- `subpages.ShopSettingsFragment`
  - shop list is Room
  - delete: `DELETE shops/{id}`
  - edit/settings saves are local UI only

there is a `ShopDetailsPagerAdapter` for Overview/Subscription/Statistics/Settings, but I couldn't find any code that instantiates this adapter. ShopStatistics is separately reachable from the nav graph; the other 3 look orphaned for now.

# compose screen files already there

under commonMain on `dev`:

- `TransactionsScreenDemo.kt` - hardcoded demo data only. `App()` renders this; iOS entry currently gets this demo screen.
- `ui/theme` and `ui/components` already have the Poppins theme, colors, shapes and reusable Compose bits.

android screens are still XML/fragments on this branch. `App()` is not wired into an android activity yet. `main` has compose dashboard + transfer list/create files, so check there if we need to bring any of that work over.

## stale stuff

- `nav_graph_subscription.xml` looks unused (`activity_main` only loads `nav_graph.xml`). it duplicates 6 subscription destinations and one points to a nonexistent `fragments.subscription.PaymentStatusFragment` package. treat it as stale until confirmed otherwise.
- main graph has 41 destination declarations, but FAQ detail class is declared twice and UsageStatistics class name is broken.
- `paymentHistoryFragment` is a stub while `SubscriptionHistoryFragment` is the real API-backed list nested in a pager.
- `OseboApiService` contains nice-looking endpoints for suppliers, sessions, contact, FAQs, dashboards etc, but it has no consumer. those are not current screen endpoints.
- product create/delete Retrofit methods exist but current UI saves/deletes locally. only product refresh and update are actually networked.
- shop update Retrofit method exists but EditShop fakes success. shop lists in ShopsActivity/ShopsFragment/ShopDetails are cache reads; refresh mostly happens in MainActivity/MainDashboard.
- `AccountViewModel` has account/two-factor/notification/deactivate/delete calls but no screen injects that ViewModel.
- primary ApiService also defines notifications, support FAQs/tickets and report-download endpoints; no current screen calls them.
- endpoint naming is mixed (`api/v1/...`, `users/...`, `roles`, `shops/{id}`), so don't “clean up” paths in Compose code without backend confirmation.
