using System.ComponentModel.DataAnnotations;

namespace Uturn.Models
{
	public class Vehicle
	{
        public string key { get; set; } = "";

        [Required]
        public string model { get; set; } = "";

        [Required]
        public string brand { get; set; } = "";

        [Required]
        public string type { get; set; } = "";

        [Required]
        public string numberPlate { get; set; } = "";

        public string currentLocation { get; set; } = "";

        public bool isAvailable { get; set; } = true;
    }
}

