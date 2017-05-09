import {Component, OnInit} from "@angular/core";
import {MdSnackBar, MdSnackBarRef} from "@angular/material";
import {ScorecardSection, FieldSection, TitleSection, ParagraphSection, Scorecard, NullWhen} from "../scorecard";
import {ScorecardService} from "../scorecard.service";
import {Game} from "../game";
import {GameService} from "../game.service";
import {EventService} from "../event.service";
import {ApiRootService} from "../api-root.service";
import {ApiRoot} from "../api-root";
import {ResultService} from "../result.service";
import {Match} from "../match";
import {MatchService} from "../match.service";
import {Event} from "../event";
import {Observable} from "rxjs";
import {ResultSubmission, ScorecardFieldResult} from "../result";

@Component({
             selector: 'scout-form',
             templateUrl: 'scout.component.html',
             styleUrls: ['scout.component.css']
           })
export class ScoutComponent implements OnInit {
  title = 'app works!';
  games: Game[] = [];
  selectedGame: Game = null;
  events: Event[] = [];
  eventsFetched:boolean=false;
  selectedEvent: Event = null;
  noSelectedEventSnackBar: MdSnackBarRef<any> = null;
  noSelectedEventSnackBarShown: boolean = false;
  scorecard: Scorecard = null;

  sectionModels: ScorecardSection[] = [];

  robotNumber: number;
  robotNumberMissingErrorShown: boolean = false;

  matchNumber: number;
  matchType: string;
  matchNumberMissingErrorShown: boolean = false;
  matchTypeMissingSnackBar: MdSnackBarRef<any> = null;
  matchTypeMissingSnackBarShown: boolean = false;

  apiRoot: ApiRoot;

  constructor(private scorecardService: ScorecardService, private gameService: GameService,
              private eventService: EventService, private apiRootService: ApiRootService,
              private resultService: ResultService, private matchService: MatchService,
              private snackBar: MdSnackBar) {}

  ngOnInit() {
    this.apiRootService.getRoot().subscribe(root => {
      this.apiRoot = root;
      console.log(JSON.stringify(root));

      this.gameService.getAllGames(root).subscribe(games => {
        this.games = games;
        console.log(JSON.stringify(games));
      });
    });
  }

  isFieldSection(section: ScorecardSection): boolean {return section.sectionType == "field"}

  toFieldSectionModel(section: ScorecardSection): FieldSectionModel {return <FieldSectionModel><any>section}

  isTitleSection(section: ScorecardSection): boolean {return section.sectionType == "title"}

  toTitleSection(section: ScorecardSection): TitleSection {return <TitleSection><any>section}

  isParagraphSection(section: ScorecardSection): boolean {return section.sectionType == "paragraph"}

  toParagraphSection(section: ScorecardSection): ParagraphSection {return <ParagraphSection><any>section}

  selectedGameChange(): void {
    this.eventsFetched = false;
    this.scorecardService.getScorecardsByGame(this.selectedGame).subscribe(
      scorecards => {
        this.scorecard = scorecards[0];
        console.log(JSON.stringify(scorecards));
        this.sectionModels = this.scorecard.sections.map(
          (section) => {
            if (this.isFieldSection(section))return new FieldSectionModel(section);
            else return section;
          }).sort((section1, section2) => {
          if (section1.index == section2.index) return 0;
          return section1.index > section2.index ? 1 : -1;
        });
      },
      error => console.log(error.toString()));

    this.eventService.getEventsByGame(this.selectedGame).subscribe(
      events => {
        this.events = events;
        this.eventsFetched = true;
      },
      error => console.log(error.toString())
    );

    console.log("changed");
  }

  validateRobotNumberFieldAndShowError(): boolean {
    return !(this.robotNumberMissingErrorShown = (this.robotNumber == null));
  }

  validateSelectedEventAndShowError(): boolean {
    if (this.selectedEvent == null && !this.matchTypeMissingSnackBarShown) {
      this.noSelectedEventSnackBarShown = true;
      this.noSelectedEventSnackBar = this.snackBar.open("Please select an event", "", {
        duration: 2000
      });
      this.noSelectedEventSnackBar.afterDismissed()
          .subscribe(() => {}, () => {}, () => this.noSelectedEventSnackBarShown = false);
    }
    return !(this.selectedEvent == null);
  }

  validateMatchNumberFieldAndShowError(): boolean {
    return !(this.matchNumberMissingErrorShown = (this.matchNumber == null));
  }

  validateMatchTypeAndShowError(): boolean {
    if (this.matchType == null) {
      this.matchTypeMissingSnackBarShown = true;
      this.matchTypeMissingSnackBar = this.snackBar.open("Please select a match type", "", {
        duration: 2000
      });
      this.matchTypeMissingSnackBar.afterDismissed()
          .subscribe(() => {}, () => {}, () => this.matchTypeMissingSnackBarShown = false);
    }
    return !(this.matchType == null);
  }

  submitButtonPressed(): void {
    let currentMatch: Match = null;
    let robotNumberValid = this.validateRobotNumberFieldAndShowError();
    let selectedEventValid = this.validateSelectedEventAndShowError();
    let matchNumberValid = this.validateMatchNumberFieldAndShowError();
    let matchTypeValid = this.validateMatchTypeAndShowError();
    if (robotNumberValid && selectedEventValid && matchNumberValid && matchTypeValid) {
      this.matchService.getMatches(this.selectedEvent)
          .flatMap(matches => Observable.from(matches))
          .filter(match => match.number == this.matchNumber)
          .filter(match => match.type == this.matchType).subscribe(
        match => currentMatch = match,
        error => {},
        () => {
          let submitResult = () => {
            this.resultService.submitResult(new ResultSubmission({
              scorecard: this.scorecard.id,
              scores: this.sectionModels.filter(section => this.isFieldSection(section))
                          .map(section => this.toFieldSectionModel(section))
                          .map(model => new ScorecardFieldResult({
                            score: model.type=="BOOLEAN"?(model.checked?1:0):((model.optional && (model.checked === (model.nullWhen === NullWhen.CHECKED))) ? null : model.value),
                            field: {
                              id: model.id,
                              sectionType: model.sectionType
                            }
                          })),
              robot: {
                number: this.robotNumber
              }
            }), currentMatch).subscribe(
              (result) => {
                console.log(JSON.stringify(result));
                this.snackBar.open("Submission Successful", "", {
                  duration: 2000
                });
                this.clearScorecard();
              }
            );
          };
          if (currentMatch == null) {
            this.matchService
                .createMatch(this.selectedEvent, new Match({number: this.matchNumber, type: this.matchType}))
                .subscribe(
                  (match) => {
                    currentMatch = match;
                    submitResult();
                  }
                )
          }
          else {
            submitResult();
          }
        }
      );
    }
  }

  clearScorecard(): void {
    this.sectionModels.filter(this.isFieldSection)
        .map(this.toFieldSectionModel).forEach(section => {
      section.value = 0;
      section.checked = false;
    });
    this.robotNumber = null;
    this.matchNumber = null;
  }

  log(message: any): void {console.log(message);}

}

/*@Pipe({name:"sortScorecardSection",pure:true})
 export class sortScorecardSectionPipe implements PipeTransform{
 transform(value:ScorecardSection[]):ScorecardSection[]{
 return value.sort((section1,section2)=>{
 if(section1.index==section2.index) return 0;
 return section1.index>section2.index?1:-1;
 })
 }
 }*/

class FieldSectionModel extends FieldSection {
  value: number = 0;
  checked: boolean = false;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
