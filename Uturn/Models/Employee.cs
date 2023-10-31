using System.ComponentModel.DataAnnotations;

namespace Uturn.Models
{
    public class Employee
    {
        public string key { get; set; } = "";

        [Required]
        [EmailAddress]
        public string Email { get; set; }

        public string FCMToken { get; set; } = "";
        public string Picture { get; set; } = "";
        public string Role { get; set; } = "User";
        public string state { get; set; } = "";
        public string password { get; set; } = "12345678";
        public bool typing { get; set; } = false;

        public string name { get; set; }

        public string surname { get; set; }
    }
}