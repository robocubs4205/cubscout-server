import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {ArrayWithSizePipe} from "./array-with-size.pipe";
import {CurrentEventsPipe} from "./current-events.pipe";

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [
    ArrayWithSizePipe,
    CurrentEventsPipe
  ],
  exports:[
    ArrayWithSizePipe,
    CurrentEventsPipe
  ]
})
export class RobocubsPipesModule { }
