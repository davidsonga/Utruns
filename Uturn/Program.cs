using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;

var builder = WebApplication.CreateBuilder(args);

string pathToKey = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "utrun-8aee7-firebase-adminsdk-apvtg-6a894b7e8c.json");
FirebaseApp.Create(new AppOptions
{
    Credential = GoogleCredential.FromFile(pathToKey),
    ProjectId = "utrun-8aee7",
});

// Add services to the container.
builder.Services.AddControllersWithViews();

// Add Sidebar menu json file
builder.Configuration.AddJsonFile("sidebar.json", optional: true, reloadOnChange: true);

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseDeveloperExceptionPage();
}
else
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

app.UseEndpoints(endpoints =>
{
    endpoints.MapControllerRoute(
        name: "default",
        pattern: "{controller=Home}/{action=Index}/{id?}");

    endpoints.MapControllers();
});

app.Run();
