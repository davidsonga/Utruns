using System.ComponentModel.DataAnnotations;

namespace Uturn.ViewModels
{
	public class LocationViewModel
	{
        [Required]
        public string name { get; set; } = "";

        [Required]
        public string type { get; set; } = "";

        [Required]
        public string address { get; set; } = "";
    }
}

